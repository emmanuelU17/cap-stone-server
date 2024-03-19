<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/service/WorkerProductDetailService.java
package dev.webserver.product.service;

import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.CustomServerError;
import dev.webserver.exception.DuplicateException;
import dev.webserver.product.dto.ProductDetailDto;
import dev.webserver.product.dto.UpdateProductDetailDto;
import dev.webserver.product.entity.Product;
import dev.webserver.product.entity.ProductDetail;
import dev.webserver.product.repository.ProductDetailRepo;
import dev.webserver.product.repository.ProductImageRepo;
import dev.webserver.product.repository.ProductRepo;
import dev.webserver.product.response.DetailResponse;
import dev.webserver.util.CustomUtil;
========
package dev.capstone.product.service;

import dev.capstone.exception.CustomNotFoundException;
import dev.capstone.exception.CustomServerError;
import dev.capstone.exception.DuplicateException;
import dev.capstone.product.dto.ProductDetailDto;
import dev.capstone.product.dto.UpdateProductDetailDto;
import dev.capstone.product.entity.Product;
import dev.capstone.product.entity.ProductDetail;
import dev.capstone.product.repository.ProductDetailRepo;
import dev.capstone.product.repository.ProductImageRepo;
import dev.capstone.product.repository.ProductRepo;
import dev.capstone.product.response.DetailResponse;
import dev.capstone.util.CustomUtil;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/service/WorkerProductDetailService.java
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class WorkerProductDetailService {

    @Value(value = "${aws.bucket}")
    @Setter
    private String BUCKET;

    private final ProductDetailRepo detailRepo;
    private final ProductSkuService skuService;
    private final ProductImageRepo imageRepo;
    private final ProductRepo productRepo;
    private final HelperService helperService;

    /**
     * Retrieves {@link ProductDetail} asynchronously by the specified {@link Product} uuid.
     * Each {@link ProductDetail} consists of various attributes such as visibility, color,
     * URLs, and variants.
     *
     * @param uuid The uuid of the {@link Product} for which details are to be retrieved.
     * @return A {@link CompletableFuture} containing a list of {@link DetailResponse} objects,
     * representing the {@link ProductDetail}. Each {@link DetailResponse} object encapsulates
     * information such as visibility, color, URLs, and variants.
     * @throws CustomServerError if an error occurs during the asynchronous processing.
     */
    public CompletableFuture<List<DetailResponse>> productDetailsByProductUuid(String uuid) {
        var futures = detailRepo
                .productDetailsByProductUuidAdminFront(uuid)
                .stream()
                .map(pojo -> (Supplier<DetailResponse>) () -> {
                    var req = Arrays
                            .stream(pojo.getImage().split(","))
                            .map(key -> (Supplier<String>) () -> helperService.preSignedUrl(BUCKET, key))
                            .toList();

                    var urls = CustomUtil
                            .asynchronousTasks(req, WorkerProductDetailService.class)
                            .thenApply(v -> v.stream().map(Supplier::get).toList())
                            .join();

                    var variants = CustomUtil
                            .toVariantArray(pojo.getVariants(), WorkerProductDetailService.class);

                    return new DetailResponse(pojo.getVisible(), pojo.getColour(), urls, variants);
                })
                .toList();

        return CustomUtil.asynchronousTasks(futures, WorkerProductDetailService.class)
                .thenApply(v -> v.stream().map(Supplier::get).toList());
    }

    /**
     * Create new {@link ProductDetail}
     *
     * @param dto of type {@link ProductDetailDto}.
     * @throws CustomNotFoundException is thrown if product uuid does not exist.
     * @throws DuplicateException      is thrown if product colour exists.
     */
    @Transactional(rollbackFor = CustomNotFoundException.class)
    public void create(ProductDetailDto dto, MultipartFile[] multipartFiles) {
        var product = this.productRepo
                .productByUuid(dto.uuid())
                .orElseThrow(() -> new CustomNotFoundException("Product does not exist"));

        Optional<ProductDetail> exist = this.detailRepo.productDetailByColour(dto.colour());

        if (exist.isPresent()) {
            this.skuService.save(dto.sizeInventory(), exist.get());
            return;
        }

        // validate multipartFiles are all images
        var files = this.helperService.customMultiPartFiles(multipartFiles, new StringBuilder());

        // save ProductDetail
        var detail = ProductDetail.builder()
                .product(product)
                .colour(dto.colour())
                .createAt(CustomUtil.toUTC(new Date()))
                .isVisible(dto.visible())
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // save ProductDetail
        var saved = this.detailRepo.save(detail);

        // save ProductSKU
        this.skuService.save(dto.sizeInventory(), saved);

        this.helperService.saveProductImages(detail, files, BUCKET);
    }

    /**
     * Updates a ProductDetail based on ProductSKU sku
     *
     * @param dto of type DetailDTO
     */
    @Transactional
    public void update(final UpdateProductDetailDto dto) {
        this.detailRepo.updateProductSkuAndProductDetailByProductSku(
                dto.sku(),
                dto.colour(),
                dto.isVisible(),
                dto.qty(),
                dto.size()
        );
    }

    /**
     * Permanently deletes ProductDetail and its relationship with ProductImages and ProductSKU
     *
     * @param sku is ProductSKU property. ProductSKU has a many to 1 relationship with ProductDetail
     * @throws CustomNotFoundException is thrown when sku does not exist
     * @throws S3Exception             is thrown when deleting from s3
     */
    @Transactional
    public void delete(final String sku) {
        var detail = productDetailByProductSku(sku);

        var images = this.imageRepo.imagesByProductDetailId(detail.getProductDetailId());

        List<ObjectIdentifier> keys = images //
                .stream() //
                .map(image -> ObjectIdentifier.builder().key(image.getImageKey()).build())
                .toList();

        if (!keys.isEmpty()) {
            this.helperService.deleteFromS3(keys, BUCKET);
        }

        // Remove detail from Product and Save Product
        this.detailRepo.delete(detail);
    }

    /**
     * Save ProductDetail
     */
    public ProductDetail productDetail(
            final Product product,
            final String colour,
            final boolean visible,
            final Date date
    ) {
        var detail = ProductDetail.builder()
                .product(product)
                .colour(colour)
                .createAt(date)
                .isVisible(visible)
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // Save ProductDetail
        return this.detailRepo.save(detail);
    }

    /**
     * Find ProductDetail by sku
     */
    public ProductDetail productDetailByProductSku(final String sku) {
        return this.detailRepo
                .productDetailByProductSku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

}
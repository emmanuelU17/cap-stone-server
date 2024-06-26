package dev.webserver.product.service;

import dev.webserver.aws.S3Service;
import dev.webserver.exception.CustomServerError;
import dev.webserver.product.entity.ProductDetail;
import dev.webserver.product.entity.ProductImage;
import dev.webserver.product.repository.ProductImageRepo;
import dev.webserver.product.response.CustomMultiPart;
import dev.webserver.util.CustomUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class HelperService {

    private final ProductImageRepo repository;
    private final S3Service service;

    public String preSignedUrl(@NotNull String bucket, @NotNull String key) {
        return this.service.preSignedUrl(bucket, key);
    }

    public void deleteFromS3(@NotNull List<ObjectIdentifier> keys, @NotNull String bucket) {
        this.service.deleteFromS3(keys, bucket);
    }

    /**
     * Concurrently uploads multiple product images to Amazon S3 and
     * saves their details to the database. This method leverages
     * multithreading by creating multiple callables, each responsible
     * for uploading and saving one image.
     *
     * @param detail The {@link ProductDetail} associated with the images.
     * @param files An array of {@link CustomMultiPart} objects representing
     *              the images to be uploaded.
     * @param bucket The name of the Amazon S3 bucket to which the images will
     *               be uploaded.
     * @throws CustomServerError if there is an error executing the tasks.
     */
    public void saveProductImages(@NotNull ProductDetail detail, @NotNull CustomMultiPart[] files, @NotNull String bucket) {
        var future = Arrays.stream(files)
                .map(file -> (Supplier<CustomMultiPart>) () -> {
                    service.uploadToS3(file.file(), file.metadata(), bucket, file.key());
                    return file;
                })
                .toList();

        // save all images as long as we have successfully saved to s3
        CustomUtil.asynchronousTasks(future, HelperService.class)
                .join()
                .forEach(e -> {
                    CustomMultiPart obj = e.get();
                    this.repository.save(new ProductImage(obj.key(), obj.file().getAbsolutePath(), detail));
                });
    }

}
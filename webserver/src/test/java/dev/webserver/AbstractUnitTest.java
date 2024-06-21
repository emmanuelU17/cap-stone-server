package dev.webserver;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({ MockitoExtension.class, SpringExtension.class })
@TestPropertySource(locations = "classpath:application.properties")
public abstract class AbstractUnitTest{ }
<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/exception/ExceptionResponse.java
package dev.webserver.exception;
========
package dev.capstone.exception;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/exception/ExceptionResponse.java

import org.springframework.http.HttpStatus;

public record ExceptionResponse(String message, HttpStatus status) { }
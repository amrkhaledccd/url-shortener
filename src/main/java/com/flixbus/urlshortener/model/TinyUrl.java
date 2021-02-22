package com.flixbus.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TinyUrl {
    @Id
    private String id;

    @Version
    private long version;

    @Indexed(name = "index_alias", unique = true)
    @NotNull(message = "alias cannot be null")
    private String alias;

    @NotNull(message = "originalUrl cannot be null")
    private String originalUrl;

    private String userId;
    private long redirectionCount;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expirationDate;

}

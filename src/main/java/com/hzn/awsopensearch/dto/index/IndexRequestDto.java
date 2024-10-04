package com.hzn.awsopensearch.dto.index;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Setter
@Getter
@Schema (title = "인덱스 요청 객체")
public class IndexRequestDto {
	@NotBlank
	@Schema (title = "인덱스 명칭", example = "test-index", requiredMode = RequiredMode.REQUIRED)
	private String indexName;
}

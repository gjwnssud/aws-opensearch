package com.hzn.awsopensearch.dto.index;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 23.
 */
@Setter
@Getter
@Schema (title = "인덱스 별칭 요청 객체")
public class AliasRequest extends IndexRequest {
	@NotNull
	@Schema (title = "별칭", example = "ntt-search-index", requiredMode = RequiredMode.REQUIRED)
	private String aliasName;
}

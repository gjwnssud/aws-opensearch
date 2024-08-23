package com.hzn.awsopensearch.dto.index;

import com.hzn.awsopensearch.enums.IndexType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
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
public class IndexRequest {
	@NotNull
	@Schema (title = "인덱스 종류", example = "autocomplete", requiredMode = RequiredMode.REQUIRED)
	private IndexType indexType;
}

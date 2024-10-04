package com.hzn.awsopensearch.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 24.
 */
@Setter
@Getter
@Schema (title = "검색 요청 객체")
public class SearchRequestDto extends AutocompleteRequestDto {
	@NotNull
	@Min (1)
	@Schema (title = "검색 건수", example = "10", requiredMode = RequiredMode.REQUIRED, type = "integer")
	private Integer size;
}

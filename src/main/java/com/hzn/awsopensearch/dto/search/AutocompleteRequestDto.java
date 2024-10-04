package com.hzn.awsopensearch.dto.search;

import com.hzn.awsopensearch.dto.index.IndexRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
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
@Schema (title = "자동 완성 요청 객체")
public class AutocompleteRequestDto extends IndexRequestDto {
	@NotBlank
	@Schema (title = "검색 키워드", requiredMode = RequiredMode.REQUIRED)
	private String keyword;
}

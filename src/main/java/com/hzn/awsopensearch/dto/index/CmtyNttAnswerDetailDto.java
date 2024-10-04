package com.hzn.awsopensearch.dto.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 11.
 */
@Setter
@Getter
@JsonInclude (JsonInclude.Include.NON_NULL)
public class CmtyNttAnswerDetailDto {
	private Integer cmtyNttAnswerSn;
	private String nttAnswerCn;
	private String nttAnswerRegistDt;
	private String sysRegistDt;
	private String sysUpdtDt;
	private String deleteAt;
}

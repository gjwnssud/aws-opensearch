package com.hzn.awsopensearch.vo.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 11.
 */
@Getter
@Setter
@JsonInclude (JsonInclude.Include.NON_NULL)
public class UserCmtyNttBlckgInfo {
	private Integer userNttBlckgSn;
	private Integer userSn;
	private String sysRegistDt;
	private String sysUpdtDt;
	private String deleteAt;
}

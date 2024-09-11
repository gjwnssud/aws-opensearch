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
@Setter
@Getter
@JsonInclude (JsonInclude.Include.NON_NULL)
public class UserCmtyFrendBlckgInfo {
	private Integer userFrendBlckgSn;
	private Integer userSn;
	private Integer blckgUserSn;
	private String sysRegistDt;
	private String sysUpdtDt;
	private String deleteAt;
}

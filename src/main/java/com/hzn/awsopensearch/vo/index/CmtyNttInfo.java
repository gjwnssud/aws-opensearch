package com.hzn.awsopensearch.vo.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 11.
 */
@Setter
@Getter
@JsonInclude (JsonInclude.Include.NON_NULL)
@ToString
@NoArgsConstructor
public class CmtyNttInfo {
	private Integer cmtyNttSn;
	private String nttSj;
	private String nttCn;
	private String nttRegistDt;
	private String prmbrshCntntsAt;
	private Integer cmtyNttCtgrySn;
	private String nttOthbcScopeCode;
	private Integer userSn;
	private String nttNoticeAt;
	private String nttNoticeBeginDt;
	private String nttNoticeEndDt;
	private String sysRegistDt;
	private String sysUpdtDt;
	private String deleteAt;

	@JsonInclude (Include.NON_EMPTY)
	private List<CmtyNttAnswerDetail> cmtyNttAnswers;
	@JsonInclude (Include.NON_EMPTY)
	private List<UserCmtyNttBlckgInfo> nttBlckgInfo;
	@JsonInclude (Include.NON_EMPTY)
	private List<UserCmtyFrendBlckgInfo> cmtyFrendBlckgInfo;
	@JsonInclude (Include.NON_EMPTY)
	private List<UserCmtyFrendBlckgInfo> cmtyFrendBlckgInfo2;
}

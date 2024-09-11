package com.hzn.awsopensearch.vo.index;

import com.hzn.awsopensearch.dto.PageRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 11.
 */
@Getter
@SuperBuilder
public class CmtyNttRequest extends PageRequest {
	private List<Integer> cmtyNttSnList;

	private LocalDateTime cmtyNttMaxSysRegistDt;
	private LocalDateTime cmtyNttMaxSysUpdtDt;
	private LocalDateTime cmtyNttAnswersMaxSysRegistDt;
	private LocalDateTime cmtyNttAnswersMaxSysUpdtDt;
	private LocalDateTime nttBlckgInfoMaxSysRegistDt;
	private LocalDateTime nttBlckgInfoMaxSysUpdtDt;
	private LocalDateTime cmtyFrendBlckgInfoMaxSysRegistDt;
	private LocalDateTime cmtyFrendBlckgInfoMaxSysUpdtDt;
	private LocalDateTime cmtyFrendBlckgInfo2MaxSysRegistDt;
	private LocalDateTime cmtyFrendBlckgInfo2MaxSysUpdtDt;
}

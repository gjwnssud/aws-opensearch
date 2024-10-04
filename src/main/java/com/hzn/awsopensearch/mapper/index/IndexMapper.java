package com.hzn.awsopensearch.mapper.index;

import com.hzn.awsopensearch.dto.index.CmtyNttInfoDto;
import com.hzn.awsopensearch.dto.index.CmtyNttRequestDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Mapper
public interface IndexMapper {
	List<CmtyNttInfoDto> getCmtyNttInfoList (CmtyNttRequestDto cmtyNttRequest);

	List<Integer> getIndexableNttSnList (CmtyNttRequestDto cmtyNttRequest);
}

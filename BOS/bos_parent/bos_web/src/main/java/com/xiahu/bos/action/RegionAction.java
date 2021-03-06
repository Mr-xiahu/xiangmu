package com.xiahu.bos.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.struts2.ServletActionContext;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.xiahu.bos.action.base.BaseAction;
import com.xiahu.bos.domain.PageBean;
import com.xiahu.bos.domain.Region;
import com.xiahu.bos.domain.Staff;
import com.xiahu.bos.domain.Subarea;
import com.xiahu.bos.service.IRegionService;
import com.xiahu.bos.utils.PinYin4jUtils;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/*
 * 区域管理的Action
 */
@Controller
@Scope("prototype")
public class RegionAction extends BaseAction<Region> {
	// 属性驱动，接收上传的文件
	private File regionFile;

	@Autowired
	private IRegionService regionService;

	public void setRegionFile(File regionFile) {
		this.regionFile = regionFile;
	}

	/*
	 * 导入文件
	 */
	@RequiresPermissions("region-importFile")
	public String importFile() throws IOException {
		List<Region> regionList = new ArrayList<Region>();
		// 使用POI解析Excel文件
		HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(regionFile));
		// 根据名称获得指定Sheet对象
		HSSFSheet hssfSheet = workbook.getSheet("Sheet1");
		for (Row row : hssfSheet) {
			int rowNum = row.getRowNum();
			if (rowNum == 0) {
				continue;
			}
			String id = row.getCell(0).getStringCellValue();
			String province = row.getCell(1).getStringCellValue();
			String city = row.getCell(2).getStringCellValue();
			String district = row.getCell(3).getStringCellValue();
			String postcode = row.getCell(4).getStringCellValue();
			// 包装一个区域对象
			Region region = new Region(id, province, city, district, postcode, null, null, null);
			province = province.substring(0, province.length() - 1);
			city = city.substring(0, city.length() - 1);
			district = district.substring(0, district.length() - 1);
			String info = province + city + district;
			String[] headByString = PinYin4jUtils.getHeadByString(info);
			String shortcode = StringUtils.join(headByString);
			// 城市编码---->>shijiazhuang
			String citycode = PinYin4jUtils.hanziToPinyin(city, "");

			region.setShortcode(shortcode);
			region.setCitycode(citycode);

			regionList.add(region);
		}
		// 批量保存
		regionService.saveBatch(regionList);
		return NONE;
	}

	// 属性驱动,接受准备删除的区域的ID
	private String ids;

	/*
	 * 批量删除区域
	 */
	@RequiresPermissions("region-delete")
	public String deleteBatch() {
		regionService.deleteBatch(ids);

		return LIST;
	}

	/*
	 * 添加区域
	 */
	@RequiresPermissions("region-add")
	public String addRegion() {
		regionService.save(model);
		return LIST;
	}

	/*
	 * 修改区域信息
	 */
	@RequiresPermissions("region-edit")
	public String editRegion() {
		Region region = regionService.findById(model.getId());
		// 使用页面提交的数据进行覆盖
		region.setProvince(model.getProvince());
		region.setCity(model.getCity());
		region.setDistrict(model.getDistrict());
		region.setCitycode(model.getPostcode());
		region.setShortcode(model.getShortcode());
		region.setCitycode(model.getCitycode());
		regionService.edit(region);
		return LIST;
	}

	/*
	 * 分页查询
	 */
	@RequiresPermissions("region-list")
	public String pageQuery() throws IOException {
		regionService.getPageBean(pageBean);
		this.java2Json(pageBean, new String[] { "currentPage", "pageSize", "detachedCriteria", "subareas" });
		return NONE;
	}

	private String q;

	/*
	 * 获取所有区域名称
	 */
	public String getAjaxList() throws IOException {
		List<Region> list = null;
		if (StringUtils.isNotBlank(q)) {
			list = regionService.findAllByQ(q);
		} else {
			list = regionService.findAll();
		}
		this.java2Json(list, new String[] { "subareas" });
		return NONE;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

}

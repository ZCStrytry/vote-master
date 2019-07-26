package com.jeecg.p3.system.web.account;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtil;
import org.apache.velocity.VelocityContext;
import org.jeecgframework.p3.core.common.utils.AjaxJson;
import org.jeecgframework.p3.core.util.MD5Util;
import org.jeecgframework.p3.core.util.SystemTools;
import org.jeecgframework.p3.core.util.plugin.ViewVelocity;
import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.PaginatedList;
import org.jeecgframework.p3.core.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeecg.p3.system.entity.JwSystemRole;
import com.jeecg.p3.system.entity.JwSystemUser;
import com.jeecg.p3.system.enums.UserTypeEnum;
import com.jeecg.p3.system.service.JwSystemRoleService;
import com.jeecg.p3.system.service.account.AccountService;
import com.jeecg.p3.system.util.Constants;
import com.jeecg.p3.system.vo.LoginUser;

/**
 * 账号管理
 * 
 * @author chenmingjin
 *
 */
@SuppressWarnings("rawtypes")
@Controller
@RequestMapping("/system/back/account")
public class AccountController extends BaseController {

	@Autowired
	private AccountService accountService;

	@Autowired
	private JwSystemRoleService jwSystemRoleService;

	/**
	 * 列表页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "list", method = { RequestMethod.GET, RequestMethod.POST })
	public void list(@ModelAttribute JwSystemUser query, HttpServletResponse response, HttpServletRequest request,
			@RequestParam(required = false, value = "pageNo", defaultValue = "1") int pageNo,
			@RequestParam(required = false, value = "pageSize", defaultValue = "10") int pageSize) throws Exception {
		PageQuery<JwSystemUser> pageQuery = new PageQuery<JwSystemUser>();
		pageQuery.setPageNo(pageNo);
		pageQuery.setPageSize(pageSize);
		pageQuery.setQuery(query);
		LoginUser user = (LoginUser) request.getSession().getAttribute(Constants.OPERATE_WEB_LOGIN_USER);
		String userId = user.getUserId();
		if (!"admin".equals(userId)) {
			query.setAgentId(userId);
			query.setUserIds(userId);
		}
		PaginatedList pageList = SystemTools.convertPaginatedList(accountService.queryPageList(pageQuery));
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("query", query);
		velocityContext.put("loginUser", user.getUserId());
		velocityContext.put("pageInfos", pageList);
		velocityContext.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		String viewName = "system/account/account-list.vm";
		ViewVelocity.view(request, response, viewName, velocityContext);
	}

	/**
	 * add
	 * 
	 * @return
	 */
	@GetMapping(value = "add")
	public void getAdd(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) throws Exception {
		VelocityContext velocityContext = new VelocityContext();
		String viewName = "system/account/account-add.vm";
		LoginUser user = (LoginUser) seesion.getAttribute(Constants.OPERATE_WEB_LOGIN_USER);
		String userType = user.getUserTyp();
		List<Enum<UserTypeEnum>> list = UserTypeEnum.getAllTypeList();
		if (StringUtil.isNotBlank(userType)) {
			for (int i = 0; i < Integer.parseInt(userType) + 1; i++) {
				list.remove(0);
			}
		}
		velocityContext.put("list", list);
		ViewVelocity.view(request, response, viewName, velocityContext);
	}

	/**
	 * add
	 * 
	 * @return
	 */
	@PostMapping(value = "add")
	@ResponseBody
	public AjaxJson postAdd(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
		AjaxJson j = new AjaxJson();
		try {
			String accountNo = request.getParameter("accountNo");
			LoginUser u = this.accountService.queryUserByUserId(accountNo);
			if (null != u) {
				j.setSuccess(false);
				j.setMsg("此账号已存在");
				return j;
			}
			String userName = request.getParameter("userName");
			String password = request.getParameter("password");
			String userType = request.getParameter("userType");
			LoginUser user = (LoginUser) session.getAttribute(Constants.OPERATE_WEB_LOGIN_USER);
			JwSystemUser jwSystemUser = new JwSystemUser();
			jwSystemUser.setUserId(accountNo);
			jwSystemUser.setUserName(userName);
			jwSystemUser.setUserTyp(userType);
			if (!StringUtil.isEmpty(password)) {
				jwSystemUser.setPassword(MD5Util.MD5Encode(password, "utf-8"));
			}
			jwSystemUser.setUserId(accountNo);
			jwSystemUser.setCreator(user.getUserName());
			jwSystemUser.setUserStat("NORMAL");
			jwSystemUser.setAgentId(user.getUserId());
			// 授权
			JwSystemRole jwSystemRole = jwSystemRoleService.getByRoleId(userType);
			List<String> list = new ArrayList<String>();
			list.add(jwSystemRole.getId() + "");
			this.accountService.doAdd(jwSystemUser, list);
			j.setSuccess(true);
			j.setMsg("保存成功");
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("保存失败");
			log.error("保存出错", e);
		}
		return j;
	}

	/**
	 * update
	 * 
	 * @return
	 */
	@GetMapping(value = "update")
	public void toUpdate(HttpServletResponse response, HttpServletRequest request, HttpSession seesion)
			throws Exception {
		String id = request.getParameter("id");
		JwSystemUser updateUser = this.accountService.queryById(Long.parseLong(id));
		VelocityContext velocityContext = new VelocityContext();
		String viewName = "system/account/account-update.vm";
		LoginUser user = (LoginUser) seesion.getAttribute(Constants.OPERATE_WEB_LOGIN_USER);
		String userType = user.getUserTyp();
		List<Enum<UserTypeEnum>> list = UserTypeEnum.getAllTypeList();
		if (StringUtil.isNotBlank(userType)) {
			for (int i = 0; i < Integer.parseInt(userType) + 1; i++) {
				list.remove(0);
			}
		}
		velocityContext.put("list", list);
		velocityContext.put("user", updateUser);
		ViewVelocity.view(request, response, viewName, velocityContext);
	}

	/**
	 * update
	 * 
	 * @return
	 */
	@PostMapping(value = "update")
	@ResponseBody
	public AjaxJson update(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) {
		AjaxJson j = new AjaxJson();
		try {
			String id = request.getParameter("id");
			String accountNo = request.getParameter("accountNo");
			LoginUser u = this.accountService.queryUserByUserId(accountNo);
			if (null != u) {
				j.setSuccess(false);
				j.setMsg("此账号已存在");
				return j;
			}
			String userName = request.getParameter("userName");
			String userType = request.getParameter("userType");
			LoginUser user = (LoginUser) seesion.getAttribute(Constants.OPERATE_WEB_LOGIN_USER);
			JwSystemUser jwSystemUser = new JwSystemUser();
			jwSystemUser.setUserName(userName);
			jwSystemUser.setUserTyp(userType);
			jwSystemUser.setUserId(accountNo);
			jwSystemUser.setEditor(user.getUserName());
			jwSystemUser.setId(Long.parseLong(id));
			this.accountService.doEdit(jwSystemUser, null);
			j.setSuccess(true);
			j.setMsg("修改成功");
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("修改失败");
			log.error("修改出错", e);
		}
		return j;
	}

	/**
	 * delete
	 * 
	 * @return
	 */
	@GetMapping(value = "delete")
	@ResponseBody
	public AjaxJson delete(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) {
		AjaxJson j = new AjaxJson();
		try {
			String id = request.getParameter("id");
			this.accountService.doDelete(Long.parseLong(id));
			j.setSuccess(true);
			j.setMsg("删除成功");
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("删除失败");
			log.error("删除出错", e);
		}
		return j;
	}

	/**
	 * reset
	 * 
	 * @return
	 */
	@GetMapping(value = "reset")
	@ResponseBody
	public AjaxJson reset(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) {
		AjaxJson j = new AjaxJson();
		try {
			String id = request.getParameter("id");
			String password = request.getParameter("password");
			JwSystemUser user = new JwSystemUser();
			user.setId(Long.parseLong(id));
			if (StringUtil.isNotBlank(password)) {
				user.setPassword(MD5Util.MD5Encode(password, "utf-8"));
				this.accountService.doEdit(user, null);
				j.setSuccess(true);
				j.setMsg("重置密码成功");
			}
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("重置密码失败");
			log.error("重置密码出错", e);
		}
		return j;
	}

	/**
	 * updatePwd
	 * 
	 * @return
	 */
	@GetMapping(value = "updatePwd")
	public void getUpdatePwd(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) {
		try {
			VelocityContext velocityContext = new VelocityContext();
			String viewName = "system/account/account-update-pwd.vm";
			ViewVelocity.view(request, response, viewName, velocityContext);
		} catch (Exception e) {
			log.error("出错", e);
		}
	}

	/**
	 * updatePwd
	 * 
	 * @return
	 */
	@PostMapping(value = "updatePwd")
	@ResponseBody
	public AjaxJson updatePwd(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) {
		AjaxJson j = new AjaxJson();
		try {
			LoginUser user = (LoginUser) seesion.getAttribute(Constants.OPERATE_WEB_LOGIN_USER);
			Integer id = user.getId();
			String password = request.getParameter("oldPassword");
			String newPassword = request.getParameter("password");
			if (StringUtil.isBlank(password) || StringUtil.isBlank(newPassword)) {
				j.setSuccess(false);
				j.setMsg("缺失参数");
				return j;
			}
			JwSystemUser u = this.accountService.queryById(id.longValue());
			String secrtPwd = MD5Util.MD5Encode(password, "utf-8");
			if (null != u) {
				if (!secrtPwd.equals(u.getPassword())) {
					j.setSuccess(false);
					j.setMsg("原始密码不正确");
					return j;
				}
			}
			JwSystemUser jwSystemUser = new JwSystemUser();
			jwSystemUser.setEditor(user.getUserName());
			jwSystemUser.setId(id.longValue());
			jwSystemUser.setPassword(MD5Util.MD5Encode(newPassword, "utf-8"));
			this.accountService.doEdit(jwSystemUser, null);
			j.setSuccess(true);
			j.setMsg("修改成功");
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("修改失败");
			log.error("修改出错", e);
		}
		return j;
	}

	/**
	 * myAchieve
	 * 
	 * @return
	 */
	@GetMapping(value = "myAchieve")
	public void myAchieve(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) {
		try {
			String id = request.getParameter("id");
			String year = request.getParameter("year");
			String month = request.getParameter("month");
			if (StringUtil.isBlank(year)) {
				year = LocalDate.now().getYear() + "";
			}
			if (StringUtil.isBlank(month)) {
				month = LocalDate.now().getMonthValue() + "";
			}
			JwSystemUser user = this.accountService.queryById(Long.parseLong(id));
			VelocityContext velocityContext = new VelocityContext();
			String viewName = "system/account/account-desc.vm";
			velocityContext.put("user", user);
			velocityContext.put("year", year);
			velocityContext.put("month", month);
			ViewVelocity.view(request, response, viewName, velocityContext);
		} catch (Exception e) {
			log.error("出错", e);
		}
	}

	/**
	 * myAchieve
	 * 
	 * @return
	 */
	@PostMapping(value = "myAchieve")
	@ResponseBody
	public AjaxJson myAchievePost(HttpServletResponse response, HttpServletRequest request, HttpSession seesion) {
		AjaxJson j = new AjaxJson();
		try {
			String id = request.getParameter("id");
			String year = request.getParameter("year");
			String month = request.getParameter("month");
			if (StringUtil.isBlank(year)) {
				year = LocalDate.now().getYear() + "";
			}
			if (StringUtil.isBlank(month)) {
				month = LocalDate.now().getMonthValue() + "";
			}
			// JwSystemUser user =
			// this.accountService.queryById(Long.parseLong(id));
			if (month.length() != 2) {
				month = "0" + month;
			}
			String date = year + "-" + month + "-01";
			int days = LocalDate.parse(date).lengthOfMonth();

			String str = "<tr>";
			for (int i = 0; i < days; i++) {
				if (i != 0 && i % 7 == 0) {
					str = str + "</tr><tr>";
				}
				str = str + "<td><p class=\"day\">" + LocalDate.parse(date).plusDays(i) + "</p><p class=\"day\">" + i
						* 100 + "</p></td>";
			}
			if (!str.endsWith("</tr>")) {
				str = str + "</tr>";
			}

			j.setObj(str);
		} catch (Exception e) {
			log.error("出错", e);
			j.setSuccess(false);
			j.setMsg("查询出错");
		}
		return j;
	}
}

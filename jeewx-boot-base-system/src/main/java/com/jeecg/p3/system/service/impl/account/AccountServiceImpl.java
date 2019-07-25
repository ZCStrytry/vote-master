package com.jeecg.p3.system.service.impl.account;

import java.util.List;

import javax.annotation.Resource;

import org.jeecgframework.p3.core.utils.common.PageList;
import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.PageQueryWrapper;
import org.jeecgframework.p3.core.utils.common.Pagenation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecg.p3.system.dao.account.AccountDao;
import com.jeecg.p3.system.entity.JwSystemUser;
import com.jeecg.p3.system.service.account.AccountService;
import com.jeecg.p3.system.vo.LoginUser;
import com.jeecg.p3.system.vo.Menu;

@Service("accountService")
public class AccountServiceImpl implements AccountService {
	@Resource
	private AccountDao accountDao;

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public void doAdd(JwSystemUser jwSystemUser, List<String> roleIds) {
		accountDao.insert(jwSystemUser);
		if (roleIds != null && roleIds.size() > 0) {
			for (String roleId : roleIds) {
				accountDao.insertUserRoleRel(jwSystemUser.getUserId(), roleId);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public void doEdit(JwSystemUser jwSystemUser, List<String> roleIds) {
		accountDao.update(jwSystemUser);
		accountDao.deleteRolesByUserId(jwSystemUser.getUserId());
		if (roleIds != null && roleIds.size() > 0) {
			for (String roleId : roleIds) {
				accountDao.insertUserRoleRel(jwSystemUser.getUserId(), roleId);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public void doDelete(Long id) {
		JwSystemUser jwSystemUser = accountDao.get(id);
		if ("admin".equals(jwSystemUser.getUserId())) {
			throw new RuntimeException("admin用户不能删除");
		}
		accountDao.deleteRolesByUserId(jwSystemUser.getUserId());
		accountDao.delete(id);
	}

	@Override
	public JwSystemUser queryById(Long id) {
		JwSystemUser jwSystemUser = accountDao.get(id);
		return jwSystemUser;
	}

	@Override
	public PageList<JwSystemUser> queryPageList(PageQuery<JwSystemUser> pageQuery) {
		PageList<JwSystemUser> result = new PageList<JwSystemUser>();
		Integer itemCount = accountDao.count(pageQuery);
		PageQueryWrapper<JwSystemUser> wrapper = new PageQueryWrapper<JwSystemUser>(pageQuery.getPageNo(),
				pageQuery.getPageSize(), itemCount, pageQuery.getQuery());
		List<JwSystemUser> list = accountDao.queryPageList(wrapper);
		Pagenation pagenation = new Pagenation(pageQuery.getPageNo(), itemCount, pageQuery.getPageSize());
		result.setPagenation(pagenation);
		result.setValues(list);
		return result;
	}

	@Override
	public List<String> queryUserRoles(String userId) {
		return accountDao.queryUserRoles(userId);
	}

	@Override
	public List<Menu> queryUserMenuAuth(List<String> roleIds) {
		return accountDao.queryUserMenuAuth(roleIds);
	}

	@Override
	public LoginUser queryUserByUserId(String userId) {
		return accountDao.queryUserByUserId(userId);
	}

	@Override
	public void doChangePassword(JwSystemUser jwSystemUser) {
		accountDao.update(jwSystemUser);
	}

	@Override
	public LoginUser queryUserByOpenid(String openid) {
		return accountDao.queryUserByOpenid(openid);
	}

	// update--begin--author: gj_shaojc--date:20180503--------for:增加代理商管理-
	@Override
	public PageList<JwSystemUser> queryAgentPageList(PageQuery<JwSystemUser> pageQuery) {
		PageList<JwSystemUser> result = new PageList<JwSystemUser>();
		Integer itemCount = accountDao.count(pageQuery);
		List<JwSystemUser> list = accountDao.queryAgentPageList(pageQuery, itemCount);
		Pagenation pagenation = new Pagenation(pageQuery.getPageNo(), itemCount, pageQuery.getPageSize());
		result.setPagenation(pagenation);
		result.setValues(list);
		return result;
	}

	// update--end--author: gj_shaojc--date:20180503--------for:增加代理商管理-

	@Override
	public String getUserChargeState(String userid) {
		return accountDao.getUserChargeState(userid);
	}

	@Override
	public Boolean validReatUserId(String userId, Integer id) {
		Integer i = accountDao.validReatUserId(userId, id);
		return (i > 0);
	}

}

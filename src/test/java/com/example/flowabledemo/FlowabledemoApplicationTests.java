package com.example.flowabledemo;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class FlowabledemoApplicationTests {

	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;

	@Autowired
	private HistoryService historyService;
	@Test
	void contextLoads() {
	}

	String staffId = "2";

	String zuzhangId = "3";

	String managerId = "10";

	/**
	 * 提交请假流程
	 */
	@Test
	void askForLeave(){
		HashMap<String,Object> map = new HashMap<>();
		map.put("leaveTask",staffId);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ask_for_leave",map);
		runtimeService.setVariable(processInstance.getId(), "name","java");
		runtimeService.setVariable(processInstance.getId(), "reason","加饭");
		runtimeService.setVariable(processInstance.getId(), "days",10);
		log.info("创建请假流程 processId:{}",processInstance.getId());
	}

	/**
	 * 提交给组长审批
	 */
	@Test
	void submitToZuzhang() {
		//员工查找到自己的任务，然后提交给组长审批
		List<Task> list = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskId().desc().list();
		for (Task task : list) {
			log.info("任务 ID：{}；任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
			Map<String, Object> map = new HashMap<>();
			//提交给组长的时候，需要指定组长的 id
			map.put("zuzhangTask", zuzhangId);
			taskService.complete(task.getId(), map);
		}
	}

	/**
	 * 组长审批-批准
	 */
	@Test
	void zuZhangApprove() {
		List<Task> list = taskService.createTaskQuery().taskAssignee(zuzhangId).orderByTaskId().desc().list();
		for (Task task : list) {

			log.info("组长 {} 在审批 {} 任务 原始 {}", task.getAssignee(), task.getId(),task.getProcessDefinitionId());
			Map<String, Object> map = new HashMap<>();
			//组长审批的时候，如果是同意，需要指定经理的 id
			map.put("managerTask", managerId);
			map.put("checkResult", "通过");
			taskService.complete(task.getId(), map);
		}
	}

	/**
	 * 组长审批-拒绝
	 */
	@Test
	void zuZhangReject() {
		List<Task> list = taskService.createTaskQuery().taskAssignee(zuzhangId).orderByTaskId().desc().list();
		for (Task task : list) {
			log.info("组长 {} 在审批 {} 任务", task.getAssignee(), task.getId());
			Map<String, Object> map = new HashMap<>();
			//组长审批的时候，如果是拒绝，就不需要指定经理的 id
			map.put("checkResult", "拒绝");
			taskService.complete(task.getId(), map);
		}
	}

	/**
	 * 经理审批自己的任务-批准
	 */
	@Test
	void managerApprove() {
		List<Task> list = taskService.createTaskQuery().taskAssignee(managerId).orderByTaskId().desc().list();
		for (Task task : list) {
			List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
							.processInstanceId(task.getProcessInstanceId()).finished().list();
			log.info(historicActivityInstances.get(0).getAssignee());

			log.info("经理 {} 在审批 {} 任务 原始{}", task.getAssignee(), task.getId(),task.getProcessInstanceId());
			Map<String, Object> map = new HashMap<>();
			map.put("checkResult", "通过");
			taskService.complete(task.getId(), map);
		}
	}

	/**
	 * 经理审批自己的任务-拒绝
	 */
	@Test
	void managerReject() {
		List<Task> list = taskService.createTaskQuery().taskAssignee(managerId).orderByTaskId().desc().list();
		for (Task task : list) {
			log.info("经理 {} 在审批 {} 任务", task.getAssignee(), task.getId());
			Map<String, Object> map = new HashMap<>();
			map.put("checkResult", "拒绝");
			taskService.complete(task.getId(), map);
		}
	}
}

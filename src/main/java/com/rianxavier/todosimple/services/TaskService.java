package com.rianxavier.todosimple.services;

import com.rianxavier.todosimple.models.Task;
import com.rianxavier.todosimple.models.User;
import com.rianxavier.todosimple.models.enums.ProfileEnum;
import com.rianxavier.todosimple.repositories.TaskRepository;
import com.rianxavier.todosimple.security.UserSpringSecuriry;
import com.rianxavier.todosimple.services.exceptions.AuthorizationException;
import com.rianxavier.todosimple.services.exceptions.DataBindingViolationException;
import com.rianxavier.todosimple.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    public Task findById(Long id) {
        Task task = this.taskRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(
                "Tarefa não encontrada! Id: " + id + ", Tipo: " + Task.class.getName()));

        UserSpringSecuriry userSpringSecuriry = UserService.authenticated();
        if (Objects.isNull(userSpringSecuriry)
                || !userSpringSecuriry.hasRole(ProfileEnum.ADMIN) && !userHasTask(userSpringSecuriry, task))
            throw new AuthorizationException("Acesso negado!");

        return task;
    }

    public List<Task> findAllByUser() {
        UserSpringSecuriry userSpringSecuriry = UserService.authenticated();
        if (Objects.isNull(userSpringSecuriry))
            throw new AuthorizationException("Acesso negado!");

        List<Task> tasks = this.taskRepository.findByUser_Id(userSpringSecuriry.getId());
        return tasks;
    }

    @Transactional
    public Task create(Task obj) {
        UserSpringSecuriry userSpringSecuriry = UserService.authenticated();
        if (Objects.isNull(userSpringSecuriry))
            throw new AuthorizationException("Acesso negado!");

        User user = this.userService.findById(userSpringSecuriry.getId());
        obj.setId(null);
        obj.setUser(user);
        obj = this.taskRepository.save(obj);
        return obj;
    }

    @Transactional
    public Task update(Task obj) {
        Task newObj = findById(obj.getId());
        newObj.setDescription(obj.getDescription());
        return this.taskRepository.save(newObj);
    }

    public void delete(Long id) {
        Task task = findById(id);

        try {
            this.taskRepository.delete(task);
        } catch (Exception e) {
            throw new DataBindingViolationException("Não foi possivel deletar a tarefa");
        }
    }

    private Boolean userHasTask(UserSpringSecuriry userSpringSecuriry, Task task) {
        return task.getUser().getId().equals(userSpringSecuriry.getId());
    }
}

package app.controllers;

import app.models.*;
import app.repositories.ProjectRepository;
import app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class ProjectController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/createProject")
    public String create(Model model) {

        model.addAttribute("project",new Project());
        model.addAttribute("view", "createProject");
        return "layout";
    }

    @PostMapping("/createProject")
    public String create(@ModelAttribute Project project, Model model){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());



        Project temp = projectRepository.findByName(project.getName());
        if (temp == null){
            if (project.getSupervisor()==null){
                project.setSupervisor(new Supervisor(user.getUsername(),user.getPassword(),user.getConfPassword(),null));
            }

            if (project.getStudents() == null){
                project.setStudents(new ArrayList<User>());
            }

            project.activate();
            projectRepository.save(project);
            return "redirect:projects";
        }else{
            model.addAttribute("view","createProjects");
            return "layout";
        }

    }

    @GetMapping("/projects")
    public String listProjects(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        Supervisor supervisor = new Supervisor("Supervisor","password","password",null);
        Iterable<Project> all = projectRepository.findAll();
        if(auth instanceof AnonymousAuthenticationToken || user.getRoleValue().equals("STUDENT")){
            List<Project> active = new ArrayList<>();
            for(Project project : all){
                if(project.isActive()){
                    active.add(project);
                }
            }
            model.addAttribute("project", active);
            model.addAttribute("view", "projects");
            return "layout";
        }
        else{
            model.addAttribute("project", all);
            model.addAttribute("view", "projects");
            return "layout";
        }
    }



    @PostMapping("/join")
    public String join(@ModelAttribute Project select, Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());

        Project selected = projectRepository.findByName(select.getName());
        if(!(selected.addStudent(user))){
            model.addAttribute("addError", true);
        }
        projectRepository.save(selected);
        user.setProject(selected);
        userRepository.save(user);

        Iterable<Project> all = projectRepository.findAll();
        List<Project> active = new ArrayList<>();
        for(Project project : all){
            if(project.isActive()){
                active.add(project);
            }
        }
        model.addAttribute("project", active);
        model.addAttribute("view", "projects");
        return "layout";
    }

    @PostMapping("/archive")
    public String archive_project(Model model, Project oper){

        Project temp = projectRepository.findByName(oper.getName());

        temp.deactivate();
        projectRepository.save(temp);

        return "redirect:projects";

    }

    @PostMapping("/unarchive")
    public String unarchive_project(Model model, Project oper){

        Project temp = projectRepository.findByName(oper.getName());

        temp.activate();
        projectRepository.save(temp);

        return "redirect:projects";

    }

    @PostMapping("/delete")
    public String deleteProject(Model model, Project delproj){

        Project temp = projectRepository.findByName(delproj.getName());

        projectRepository.delete(temp);

        return "redirect:projects";
    }

    @GetMapping("/project")
    public String accessProject(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        if (user.getProject() == null) {
            model.addAttribute("addError", true);
        } else {
            test(user.getProject());
            model.addAttribute("selected", user.getProject());
            model.addAttribute("deliverables", user.getProject().getDeliverables());
        }
        model.addAttribute("view", "project");
        return "layout";
    }

    private boolean executed = false;

    @GetMapping("/populate")
    public String populateProjects(Model model) {
        ArrayList<Program> restrictions = new ArrayList<>();
        ArrayList<User> students = new ArrayList<>();
        Supervisor supervisor = new Supervisor("Donald Bailey", "password", "password");
        Project project1 = new Project("Software Development for the Northern Nomad Tiny House", "The Nomad is equipped with several home automation features. We are looking for novel ways to apply these features in a net-zero dwelling.\n" +
                "\n" +
                "The Northern Nomad website suggests one project: the house will inform the occupants if windows are left open when the heating and cooling heat pump is running.\n" +
                "\n" +
                "Another suggested project is to develop software that enables the house to learn about the day-to-day behaviour of its occupants. For example, the house will learn when occupants typically leave and return home on week days and weekends, and will automatically regulate the heating/cooling system and lighting controls in response, depending on the season and current weather conditions. This project should be of interest to students who are interested in machine learning. We recommend that these students obtain an introduction to machine learning prior to September; for example, by completing one or more of the free machine learning courses offered by coursera and edX.\n" +
                "\n", 6, supervisor, students, restrictions);
        Project project2 = new Project("FRAME: Facial Recognition Automated Movie Evaluation", "Within the realm of Anti Piracy and media rights, enforcement validation of media files is a key, but often manual, process. The intention of this project is to use machine-learning-based facial recognition as well as targeted extraction to build an automated movie validation system for use in an end-to-end anti-piracy system. The project will be done in conjunction with the Online Piracy Detection team at Irdeto, a cyber security and technology company in Kanata. The goal is to design a proof-of-concept system. This will be done either by using cloud computing or system virtualization technologies such as Docker to simulate cloud based machines. The three main components to be developed are a movie frame extraction system, a facial recognition system and the database query segment for determining the video. The system will start by extracting screenshots from the movie to locate faces for facial recognition. The extracted screenshots will then be ran through facial recognition to determine actors and characters found in the movie. This data will then be used to query an IMDB like database to find what possible movies have those actors and characters to a given threshold of certainty. Post development, a large investigation will be necessary to determine the optimal configuration of the system; for example, the number of screenshots needed on average, the spread of screenshots, the number of faces/characters needed to validate a movie and other factors observed throughout development. Prior experience with Python, cloud computing and/or system virtualization technologies is an asset. ", 6, supervisor, students, restrictions);

        if (!executed) {
            projectRepository.save(project1);
            projectRepository.save(project2);
            executed = true;
        }

        model.addAttribute("view", "projects");
        return "layout";
    }

    private void test(Project project) {
        Date dueDate1 = new Date(2019, 4, 10, 10,30);
        Date dueDate2 = new Date(2019, 5, 10, 10,30);

        Deliverable deliverable1 = new Deliverable(dueDate1, "Progress Report", "Progress Report");
        Deliverable deliverable2 = new Deliverable(dueDate2, "Final Report", "Final Report");

        project.addDeliverable(deliverable1);
        project.addDeliverable(deliverable2);
    }
}

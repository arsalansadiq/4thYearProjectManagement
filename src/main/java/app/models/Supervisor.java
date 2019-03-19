package app.models;

import java.util.ArrayList;

public class Supervisor extends User {
    //List of projects supervised by this supervisor
    private ArrayList<Project> projects;

    public Supervisor(String username, String password, String confPassword, ArrayList<Project> projects) {
        super(username, password, confPassword, Role.SUPERVISOR);
        this.projects = projects;
    }

    public Supervisor(String username, String password, String confPassword){
        super(username, password, confPassword, Role.SUPERVISOR);
        this.projects = new ArrayList<Project>();
    }

    public void addProject(Project project){
        this.projects.add(project);
    }

    public ArrayList<Project> getProjects() {
        return projects;
    }

    public void setProjects(ArrayList<Project> projects) {
        this.projects = projects;
    }
}

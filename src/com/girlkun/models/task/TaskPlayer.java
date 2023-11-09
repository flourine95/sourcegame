package com.girlkun.models.task;



public class TaskPlayer {

    public TaskMain taskMain;
    
    public SideTask sideTask;

    public TaskPlayer() {
        this.sideTask = new SideTask();
    }
    
    public void dispose(){
        this.taskMain = null;
        this.sideTask = null;
    }

}

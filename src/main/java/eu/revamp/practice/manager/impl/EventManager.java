package eu.revamp.practice.manager.impl;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.hostedevent.HostedEvent;
import eu.revamp.practice.hostedevent.impl.SumoEvent;
import eu.revamp.practice.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class EventManager extends Manager {

    private List<HostedEvent> currentEvents;
    private long lastEvent;

    private List<HostedEvent> eventList;

    public EventManager(ManagerHandler managerHandler) {
        super(managerHandler);

        currentEvents = new ArrayList<>();
        eventList = new ArrayList<>();
        loadEvents();
        fetch();
    }

    private void loadEvents() {
        addEvent(new SumoEvent(RevampPractice.getInstance()));
    }

    private void fetch() {
        eventList.forEach(e -> {
            if (this.managerHandler.getConfigurationManager().getEventsFile().get(e.getName()) != null) {

                if (managerHandler.getConfigurationManager().getEventsFile().get(e.getName() + ".lobby") != null)
                    e.setLobby(LocationUtil.getLocationFromString(managerHandler.getConfigurationManager().getEventsFile().getString(e.getName() + ".lobby")));

                if (managerHandler.getConfigurationManager().getEventsFile().get(e.getName() + ".first") != null)
                    e.setFirst(LocationUtil.getLocationFromString(managerHandler.getConfigurationManager().getEventsFile().getString(e.getName() + ".first")));


                if (managerHandler.getConfigurationManager().getEventsFile().get(e.getName() + ".second") != null)
                    e.setSecond(LocationUtil.getLocationFromString(managerHandler.getConfigurationManager().getEventsFile().getString(e.getName() + ".second")));
            }
        });
    }

    private void addEvent(HostedEvent event) {
        eventList.add(event);
    }

    public HostedEvent getEvent(String name) {
        return eventList.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public HostedEvent getStartedEvent(String name) {
        return currentEvents.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void save() {
        eventList.forEach(e -> {
            this.managerHandler.getConfigurationManager().getEventsFile().set(e.getName(), null);

            if (e.getLobby() != null)
                this.managerHandler.getConfigurationManager().getEventsFile().set(e.getName() + ".lobby", LocationUtil.getStringFromLocation(e.getLobby()));

            if (e.getFirst() != null)
                this.managerHandler.getConfigurationManager().getEventsFile().set(e.getName() + ".first", LocationUtil.getStringFromLocation(e.getFirst()));


            if (e.getSecond() != null)
                this.managerHandler.getConfigurationManager().getEventsFile().set(e.getName() + ".second", LocationUtil.getStringFromLocation(e.getSecond()));

        });
        this.managerHandler.getConfigurationManager().saveEventsFile();
    }
}

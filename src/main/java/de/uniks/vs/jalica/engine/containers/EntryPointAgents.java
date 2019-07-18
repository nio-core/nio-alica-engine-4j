package de.uniks.vs.jalica.engine.containers;

import de.uniks.vs.jalica.engine.containers.messages.Tuple;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Created by alex on 27.07.17.
 * updated 21.6.19
 */
public class EntryPointAgents {

    // std::tuple<int64_t, std::vector<essentials::IdentifierConstPtr>> stdEntryPointRobot -> Tuple

    public ArrayList<ID> agents;
    public long entrypoint;

    public EntryPointAgents() {
        this.entrypoint = 0;
        this.agents = new ArrayList<>();
    }

    public EntryPointAgents(Tuple s) {
        this.entrypoint = (long) s.get()[0];
        this.agents = (ArrayList<ID>) s.get()[1];
    }

    public Tuple toStandard()  { return new Tuple(this.entrypoint, this.agents); }

    @Override
    public String toString() {
        String o = "";
        o += "EP: " + this.entrypoint + " Agents: ";
        //TODO: complete implmentation
        o += agents.stream().map(Object::toString).collect(Collectors.joining(", "));
//        std::copy (epr.robots.begin(), epr.robots.end(), std::ostream_iterator < essentials::IdentifierConstPtr > (o, ", "));
        o += "\n";
        return o;
    }
}

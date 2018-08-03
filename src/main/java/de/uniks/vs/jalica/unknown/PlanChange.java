package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 17.07.17.
 */
public enum PlanChange {
    NoChange, //!< NoChange occurred, rule was not applicable
    InternalChange, //!< InternalChange, change occurred but is of no interest to upper level plans
    SuccesChange, //!< SuccesChange, change occurred and led to a success, upper level can react
    FailChange //!< FailChange, change occurred and led to a failure, upper level should react
}

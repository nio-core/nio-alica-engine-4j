package de.uniks.vs.jalica.engine;

/**
 * Created by alex on 17.07.17.
 */
public enum PlanChange {
    NoChange, //!< NoChange occurred, rule was not applicable
    InternalChange, //!< InternalChange, change occurred but is of no interest teamObserver upper level plans
    SuccesChange, //!< SuccesChange, change occurred and led teamObserver a success, upper level can react
    FailChange //!< FailChange, change occurred and led teamObserver a failure, upper level should react
}

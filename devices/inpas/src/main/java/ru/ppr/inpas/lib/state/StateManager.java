package ru.ppr.inpas.lib.state;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.inpas.lib.internal.PosContext;

public class StateManager {

    private final Map<IPosState.PosState, IPosState> mStates = new HashMap<>();
    private IPosState.PosState mNextState;

    public StateManager(@NonNull final PosContext posContext) {
        mNextState = IPosState.PosState.UNKNOWN;

        final IPosState sendState = new SendState(posContext);
        final IPosState receiveState = new ReceiveState(posContext);
        final IPosState parseState = new ParseState(posContext);
        final IPosState executeState = new ExecuteState(posContext);
        final IPosState waitState = new WaitState(posContext);
        final IPosState errorState = new ErrorState(posContext);

        mStates.put(IPosState.PosState.SEND, sendState);
        mStates.put(IPosState.PosState.RECEIVE, receiveState);
        mStates.put(IPosState.PosState.PARSE, parseState);
        mStates.put(IPosState.PosState.EXECUTE, executeState);
        mStates.put(IPosState.PosState.WAIT, waitState);
        mStates.put(IPosState.PosState.ERROR, errorState);
    }

    @NonNull
    public IPosState getNext() {
        return mStates.get(mNextState);
    }

    @NonNull
    public IPosState.PosState getNextState() {
        return mNextState;
    }

    public void setNextState(@NonNull final IPosState.PosState state) {
        mNextState = state;
    }

}
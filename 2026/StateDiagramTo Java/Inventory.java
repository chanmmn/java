/**
 * Java implementation of the Inventory state machine shown in the UML
 * statechart diagram.
 *
 * States: Normal stock (initial), Low fuel, Low parts, Order fuel, Order parts,
 * and a final state.
 *
 * Transitions:
 *   [*] -> Normal stock
 *   Normal stock -> Low fuel   [fuel low in stock]
 *   Normal stock -> Low parts  [parts low in stock]
 *   Low fuel  -> Order fuel     (order_fuel() called)
 *   Low parts -> Order parts    (order_part() called)
 *   Order fuel  -> Order fuel   (re-order while waiting)
 *   Order parts -> Order parts  (re-order while waiting)
 *   Order fuel  -> Normal stock [deliver fuel]
 *   Order parts -> Normal stock [deliver parts]
 *   Normal stock -> [*]         (finish)
 */
public class Inventory {

    /** The states of the Inventory state machine. */
    public enum State {
        NORMAL_STOCK,
        LOW_FUEL,
        LOW_PARTS,
        ORDER_FUEL,
        ORDER_PARTS,
        FINISHED
    }

    private State state;

    public Inventory() {
        // [*] -> Normal stock
        this.state = State.NORMAL_STOCK;
        log("Entered initial state: " + state);
    }

    public State getState() {
        return state;
    }

    /** Notify the machine that fuel is running low. Only valid from Normal stock. */
    public void fuelLowInStock() {
        if (state == State.NORMAL_STOCK) {
            transition(State.LOW_FUEL, "fuel low in stock");
        } else {
            warnInvalid("fuelLowInStock");
        }
    }

    /** Notify the machine that parts are running low. Only valid from Normal stock. */
    public void partsLowInStock() {
        if (state == State.NORMAL_STOCK) {
            transition(State.LOW_PARTS, "parts low in stock");
        } else {
            warnInvalid("partsLowInStock");
        }
    }

    /** Places an order for fuel. Valid from Low fuel or while already Order fuel. */
    public void order_fuel() {
        if (state == State.LOW_FUEL || state == State.ORDER_FUEL) {
            transition(State.ORDER_FUEL, "order_fuel()");
        } else {
            warnInvalid("order_fuel");
        }
    }

    /** Places an order for parts. Valid from Low parts or while already Order parts. */
    public void order_part() {
        if (state == State.LOW_PARTS || state == State.ORDER_PARTS) {
            transition(State.ORDER_PARTS, "order_part()");
        } else {
            warnInvalid("order_part");
        }
    }

    /** Fuel delivery arrives; returns stock to normal. Valid only from Order fuel. */
    public void deliverFuel() {
        if (state == State.ORDER_FUEL) {
            transition(State.NORMAL_STOCK, "deliver fuel");
        } else {
            warnInvalid("deliverFuel");
        }
    }

    /** Parts delivery arrives; returns stock to normal. Valid only from Order parts. */
    public void deliverParts() {
        if (state == State.ORDER_PARTS) {
            transition(State.NORMAL_STOCK, "deliver parts");
        } else {
            warnInvalid("deliverParts");
        }
    }

    /** Terminates the state machine. Valid only from Normal stock. */
    public void finish() {
        if (state == State.NORMAL_STOCK) {
            transition(State.FINISHED, "finish -> [*]");
        } else {
            warnInvalid("finish");
        }
    }

    private void transition(State next, String trigger) {
        System.out.println(state + " --[" + trigger + "]--> " + next);
        state = next;
    }

    private void warnInvalid(String action) {
        System.out.println("Ignored '" + action + "': not valid from state " + state);
    }

    private void log(String message) {
        System.out.println(message);
    }

    /** Demonstrates the full state machine lifecycle. */
    public static void main(String[] args) {
        Inventory inventory = new Inventory();

        // Fuel runs low, gets ordered, then delivered.
        inventory.fuelLowInStock();
        inventory.order_fuel();
        inventory.order_fuel();   // re-order while waiting (self-transition)
        inventory.deliverFuel();

        // Parts run low, get ordered, then delivered.
        inventory.partsLowInStock();
        inventory.order_part();
        inventory.order_part();  // re-order while waiting (self-transition)
        inventory.deliverParts();

        // Done.
        inventory.finish();
    }
}

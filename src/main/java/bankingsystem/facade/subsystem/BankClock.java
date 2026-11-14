package bankingsystem.facade.subsystem;

// Author: Baimakhan Aibek (Facade)

import java.time.LocalDateTime;

/** Controllable clock, so demos and tests are deterministic (fraud rules depend on time). */
public class BankClock {

    private LocalDateTime now;

    public BankClock(LocalDateTime start) {
        this.now = start;
    }

    public LocalDateTime now() {
        return now;
    }

    public void advanceMinutes(long minutes) {
        now = now.plusMinutes(minutes);
    }
}

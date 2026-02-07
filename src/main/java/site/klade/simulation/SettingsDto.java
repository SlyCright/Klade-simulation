package site.klade.simulation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettingsDto {

    @Builder.Default
    private final int speciesTotal = 3;

    @Builder.Default
    private final int specimensPerSpecies = 10;

    @Builder.Default
    private final int sleepPerUpdateMillis = 100;
}

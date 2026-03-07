package site.klade.simulation;

public class SettingsDto {

    private int speciesTotal = 3;

    private int specimensPerSpecies = 10;

    private int sleepPerUpdateMillis = 100;

    SettingsDto(int speciesTotal, int specimensPerSpecies, int sleepPerUpdateMillis) {
        this.speciesTotal = speciesTotal;
        this.specimensPerSpecies = specimensPerSpecies;
        this.sleepPerUpdateMillis = sleepPerUpdateMillis;
    }

    private static int $default$speciesTotal() {
        return 3;
    }

    private static int $default$specimensPerSpecies() {
        return 10;
    }

    private static int $default$sleepPerUpdateMillis() {
        return 100;
    }

    public static SettingsDtoBuilder builder() {
        return new SettingsDtoBuilder();
    }

    public int getSpeciesTotal() {
        return this.speciesTotal;
    }

    public int getSpecimensPerSpecies() {
        return this.specimensPerSpecies;
    }

    public int getSleepPerUpdateMillis() {
        return this.sleepPerUpdateMillis;
    }

    public static class SettingsDtoBuilder {

        private int speciesTotal$value;

        private boolean speciesTotal$set;

        private int specimensPerSpecies$value;

        private boolean specimensPerSpecies$set;

        private int sleepPerUpdateMillis$value;

        private boolean sleepPerUpdateMillis$set;

        SettingsDtoBuilder() {
        }

        public SettingsDtoBuilder speciesTotal(int speciesTotal) {
            this.speciesTotal$value = speciesTotal;
            this.speciesTotal$set = true;
            return this;
        }

        public SettingsDtoBuilder specimensPerSpecies(int specimensPerSpecies) {
            this.specimensPerSpecies$value = specimensPerSpecies;
            this.specimensPerSpecies$set = true;
            return this;
        }

        public SettingsDtoBuilder sleepPerUpdateMillis(int sleepPerUpdateMillis) {
            this.sleepPerUpdateMillis$value = sleepPerUpdateMillis;
            this.sleepPerUpdateMillis$set = true;
            return this;
        }

        public SettingsDto build() {
            int speciesTotal$value = this.speciesTotal$value;
            if (!this.speciesTotal$set) {
                speciesTotal$value = SettingsDto.$default$speciesTotal();
            }
            int specimensPerSpecies$value = this.specimensPerSpecies$value;
            if (!this.specimensPerSpecies$set) {
                specimensPerSpecies$value = SettingsDto.$default$specimensPerSpecies();
            }
            int sleepPerUpdateMillis$value = this.sleepPerUpdateMillis$value;
            if (!this.sleepPerUpdateMillis$set) {
                sleepPerUpdateMillis$value = SettingsDto.$default$sleepPerUpdateMillis();
            }
            return new SettingsDto(speciesTotal$value, specimensPerSpecies$value, sleepPerUpdateMillis$value);
        }

        public String toString() {
            return "SettingsDto.SettingsDtoBuilder(speciesTotal$value=" + this.speciesTotal$value + ", specimensPerSpecies$value=" + this.specimensPerSpecies$value + ", sleepPerUpdateMillis$value=" + this.sleepPerUpdateMillis$value + ")";
        }
    }
}

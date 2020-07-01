package ru.ppr.cppk.Sounds;

public interface Ringtone {


    public void play();

    public void stop();

    public void unload();

    public enum BeepType {

        SUCCES_BEEP("SuccessBeep"),
        FAIL_BEEP("FailBeep");

        private String typeBeep = null;

        private BeepType(String typeBeep) {
            this.typeBeep = typeBeep;
        }

        public String getTypeValue() {
            return typeBeep;
        }

    }

    ;
}

package eduard.zaripov.innocamp2022.model;


/**
 * Это класс, который показывает что мы храним у устройства (Да, thing это у нас устройство)
 * Мы будем хранить id, тип, поливает ли она, работает ли она
 */

public class Thing {
    private String id;
    private String type;
    private boolean isWatering;
    private boolean isWorking;
    private double lat;
    private double lon;

    public Thing(String id, String type, boolean isWatering, boolean isWorking, double lat,double lon) {
        this.id = id;
        this.type = type;
        this.isWatering = isWatering;
        this.isWorking = isWorking;
        this.lat = lat;
        this.lon = lon;

    }

    @Override
    public String toString() {
        return id + " " + type + " " + isWatering + " " + isWorking;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public boolean isWatering() {
        return isWatering;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public double getLat( ){return lat;}

    public double getLon( ){return lon;}

    }


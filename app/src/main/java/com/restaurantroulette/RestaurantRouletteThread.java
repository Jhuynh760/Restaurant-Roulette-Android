package com.restaurantroulette;

public class RestaurantRouletteThread extends Thread{
    private RestaurantRoulette rrInstance;
    public RestaurantRouletteThread(RestaurantRoulette rrInstance){
        this.rrInstance = rrInstance;
    }

    public void run(){
        this.rrInstance.main();
    }
}

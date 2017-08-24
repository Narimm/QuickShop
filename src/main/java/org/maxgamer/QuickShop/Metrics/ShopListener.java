package org.maxgamer.QuickShop.Metrics;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.QuickShop.Shop.ShopPurchaseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.maxgamer.QuickShop.Metrics.ShopListener.ShopActions.PURCHASES;
import static org.maxgamer.QuickShop.Metrics.ShopListener.ShopActions.SALES;

public class ShopListener implements Listener {
Map<String, Integer> store = new HashMap<>();
public  Metrics.MultiLineChart chart;

    public ShopListener(Metrics metrics) {

        chart = new Metrics.MultiLineChart("", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map <String, Integer> result = new HashMap<>(store.size());
                result.putAll(store);
                result.put("server",1);
                store.clear();
                return result;
            }
        }
        );
        metrics.addCustomChart(chart);
    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    public void onPurchase(ShopPurchaseEvent e) {
        String action;
        if (e.getShop().isSelling()) {
             action= SALES.toString();

        } else {
            action = PURCHASES.toString();
        }
        Integer current = store.getOrDefault(action,0);
        store.put(action,current+e.getAmount() );
    }

    protected enum ShopActions{

        SALES("Sales"),
        PURCHASES("Purchases");

        private final String text;

        /**
         * @param text
         */
        ShopActions(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

}
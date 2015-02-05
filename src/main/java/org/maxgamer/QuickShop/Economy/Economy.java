package org.maxgamer.QuickShop.Economy;

import org.bukkit.OfflinePlayer;

public class Economy implements EconomyCore {
    private final EconomyCore core;

    public Economy(EconomyCore core) {
        this.core = core;
    }

    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     * 
     * @return True if this economy will work, false if it will not.
     */
    @Override
    public boolean isValid() {
        return core.isValid();
    }

    /**
     * Deposits a given amount of money from thin air to the given username.
     * 
     * @param name
     *            The exact (case insensitive) username to give money to
     * @param amount
     *            The amount to give them
     * @return True if success (Should be almost always)
     */
    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        if (player == null) {
            return false;
        }
        return core.deposit(player, amount);
    }

    /**
     * Withdraws a given amount of money from the given username and turns it to
     * thin air.
     * 
     * @param name
     *            The exact (case insensitive) username to take money from
     * @param amount
     *            The amount to take from them
     * @return True if success, false if they didn't have enough cash
     */
    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        return core.withdraw(player, amount);
    }

    /**
     * Transfers the given amount of money from Player1 to Player2
     * 
     * @param from
     *            The player who is paying money
     * @param to
     *            The player who is receiving money
     * @param amount
     *            The amount to transfer
     * @return true if success (Payer had enough cash, receiver was able to
     *         receive the funds)
     */
    @Override
    public boolean transfer(OfflinePlayer from, OfflinePlayer to, double amount) {
        return core.transfer(from, to, amount);
    }

    /**
     * Fetches the balance of the given account name
     * 
     * @param name
     *            The name of the account
     * @return Their current balance.
     */
    @Override
    public double getBalance(OfflinePlayer player) {
        return core.getBalance(player);
    }

    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50
     * Dollars 5 Cents
     * 
     * @param balance
     *            The given number
     * @return The balance in human readable text.
     */
    @Override
    public String format(double balance) {
        return core.format(balance);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return core.getBalance(player) >= amount;
    }

    @Override
    public String toString() {
        return core.getClass().getName().split("_")[1];
    }
}
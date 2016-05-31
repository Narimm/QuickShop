package org.maxgamer.QuickShop.Economy;

import org.bukkit.OfflinePlayer;

/**
 * @author netherfoam
 *         Represents an economy.
 */
public interface EconomyCore {
    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     * 
     * @return True if this economy will work, false if it will not.
     */
    boolean isValid();

    /**
     * Deposits a given amount of money from thin air to the given username.
     * 
     * @param player
     *            The player  to give money to
     * @param amount
     *            The amount to give them
     * @return True if success (Should be almost always)
     */
    boolean deposit(OfflinePlayer player, double amount);

    /**
     * Withdraws a given amount of money from the given username and turns it to
     * thin air.
     * 
     * @param player
     *            The player to take money from
     * @param amount
     *            The amount to take from them
     * @return True if success, false if they didn't have enough cash
     */
    boolean withdraw(OfflinePlayer player, double amount);

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
    boolean transfer(OfflinePlayer from, OfflinePlayer to, double amount);

    /**
     * Fetches the balance of the given account name
     * 
     * @param player
     *            The player of the account
     * @return Their current balance.
     */
    double getBalance(OfflinePlayer player);

    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50
     * Dollars 5 Cents
     * 
     * @param balance
     *            The given number
     * @return The balance in human readable text.
     */
    String format(double balance);
}

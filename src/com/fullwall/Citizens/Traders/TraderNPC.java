package com.fullwall.Citizens.Traders;

import java.util.ArrayList;

import com.fullwall.resources.redecouverte.NPClib.HumanNPC;

public class TraderNPC {

	private HumanNPC npc;
	private int balance;

	public TraderNPC(HumanNPC npc) {
		this.npc = npc;
	}

	private ArrayList<Buyable> buying = new ArrayList<Buyable>();
	private ArrayList<Sellable> selling = new ArrayList<Sellable>();

	public void addBuyable(Buyable buying) {
		this.buying.add(buying);
	}

	public void addSellable(Sellable selling) {
		this.selling.add(selling);
	}

	public int getBalance() {
		return this.balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public ArrayList<Buyable> getBuyables() {
		return this.buying;
	}

	public Buyable getBuyable(int itemID) {
		if (checkBuyingIntegrity()) {
			for (Buyable b : buying) {
				if (b.getBuying().getTypeId() == itemID)
					return b;
			}
		}
		return null;
	}

	public ArrayList<Sellable> getSellables() {
		return this.selling;
	}

	public Sellable getSellable(int itemID) {
		if (checkSellingIntegrity()) {
			for (Sellable s : selling) {
				if (s.getSelling().getTypeId() == itemID)
					return s;
			}
		}
		return null;
	}

	public void removeBuyable(int index, boolean item) {
		if (checkBuyingIntegrity()) {
			if (!item)
				this.buying.remove(index);
			else {
				int count = 0;
				boolean found = false;
				for (Buyable b : buying) {
					if (b.getBuying().getTypeId() == index) {
						found = true;
						break;
					}
					count += 1;
				}
				if (found)
					this.buying.remove(count);
			}
		}
	}

	public void removeSellable(int index, boolean item) {
		if (checkSellingIntegrity()) {
			if (!item)
				this.selling.remove(index);
			else {
				int count = 0;
				boolean found = false;
				for (Sellable s : selling) {
					if (s.getSelling().getTypeId() == index) {
						found = true;
						break;
					}
					count += 1;
				}
				if (found)
					this.selling.remove(count);
			}
		}
	}

	public boolean isBuyable(int itemID) {
		if (checkBuyingIntegrity()) {
			for (Buyable b : buying) {
				if (b.getBuying().getTypeId() == itemID)
					return true;
			}
		}
		return false;
	}

	public boolean isSellable(int itemID) {
		if (checkSellingIntegrity()) {
			for (Sellable s : selling) {
				if (s.getSelling().getTypeId() == itemID)
					return true;
			}
		}
		return false;
	}

	public boolean checkBuyingIntegrity() {
		if (this.buying == null || this.buying.isEmpty())
			return false;
		return true;
	}

	public boolean checkSellingIntegrity() {
		if (this.selling == null || this.selling.isEmpty())
			return false;
		return true;
	}
}

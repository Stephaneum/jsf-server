package tools;

import java.util.ArrayList;

import objects.Beitrag;

public class Pagination {
	
	final public ArrayList<Integer> numbers;
	final public int currentPage; // beginning at zero
	final public Beitrag[] beitrag; // reduced
	final public boolean isFirst, isLast;
	
	/**
	 * @param beitrag array of Beitrag
	 * @param currentPage index of current page
	 * @param amountPerPage amount of Beitrag per page
	 * @param amountPageNumbers amount of numbers shown on the bottom
	 */
	public Pagination(Beitrag[] beitrag, int currentPage, int amountPerPage, int amountPageNumbers) {
		
		if(beitrag.length == 0) {
			this.numbers = null;
			this.currentPage = 1;
			this.beitrag = new Beitrag[0];
			this.isFirst = true;
			this.isLast = true;
			return;
		}
		
		int total = beitrag.length;
		
		int siteAmount = total / amountPerPage;
		boolean hasRemainder = false;
		if(total % amountPerPage != 0) {
			siteAmount++;
			hasRemainder = true;
		}
		
		if(currentPage > siteAmount) {
			// too big
			currentPage = siteAmount;
			this.currentPage = siteAmount;
		} else {
			this.currentPage = currentPage;
		}
		
		if(siteAmount <= amountPageNumbers) {
			//shortcut
			
			this.numbers = new ArrayList<>(siteAmount);
			for(int i = 0; i < siteAmount; i++) {
				numbers.add(i+1);
			}
		} else {
			if(currentPage - amountPageNumbers / 2 < 1) {
				// near left edge
				
				this.numbers = new ArrayList<>(amountPageNumbers);
				for(int i = 0; i < amountPageNumbers; i++) {
					numbers.add(i+1);
				}
			} else if(currentPage + amountPageNumbers / 2 > siteAmount) {
				// near right edge
				
				int startingNumber = siteAmount - amountPageNumbers + 1;
				
				this.numbers = new ArrayList<>(amountPageNumbers);
				for(int i = 0; i < amountPageNumbers; i++) {
					numbers.add(startingNumber+i);
				}
			} else {
				// in the middle
				
				int startingNumber = currentPage - amountPageNumbers / 2;
				
				this.numbers = new ArrayList<>(amountPageNumbers);
				for(int i = 0; i < amountPageNumbers; i++) {
					numbers.add(startingNumber+i);
				}
			}
		}
		
		int from = (currentPage-1)*amountPerPage;
		int to = (currentPage != siteAmount || !hasRemainder) ? from + amountPerPage - 1 : beitrag.length-1;
		this.beitrag = reduce(beitrag, from, to);
		
		this.isFirst = currentPage == 1;
		this.isLast = currentPage == siteAmount;
	}
	
	// including from and to
	private Beitrag[] reduce(Beitrag[] beitrag, int from, int to) {
		Beitrag[] result = new Beitrag[to-from+1];
		for(int i = 0; i < result.length; i++) {
			result[i] = beitrag[from+i];
		}
		
		return result;
	}
	
	public ArrayList<Integer> getNumbers() {
		return numbers;
	}
	
	public boolean isFirst() {
		return isFirst;
	}
	
	public boolean isLast() {
		return isLast;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public int getPrev() {
		return currentPage-1;
	}
	
	public int getNext() {
		return currentPage+1;
	}

}

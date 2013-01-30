package edu.hit.mlg.Tools;

/**
 * One-way list. Dont't change any element of this Object returned!
 * 
 * @author Chengwu Yan
 * 
 */
public class LinkedArrayList<E> {
	private Entry<E> header = new Entry<E>(null, null, null);
	private int size = 0;

	public LinkedArrayList() {
		header.next = header.previous = header;
	}

	/**
	 * Get the first element of this list.
	 * 
	 * @return The first element of this list. Null if no any elements in this list.
	 */
	public Entry<E> getFirst() {
		Entry<E> first = header.next;
		return header != first ? first : null;
	}

	/**
	 * Get the last element of this list.
	 * 
	 * @return The last element of this list. Null if no any elements in this list.
	 */
	public Entry<E> getLast() {
		Entry<E> last = header.previous;
		return last != header ? last : null;
	}

	/**
	 * Add to the tail.
	 * 
	 * @return Return the new element added.
	 */
	public Entry<E> addLast(E e) {
		Entry<E> newEntry = new Entry<E>(e, header, header.previous);
		newEntry.previous.next = newEntry;
		newEntry.next.previous = newEntry;
		size++;
		return newEntry;
	}
	
	/**
	 * Add to the header.
	 * 
	 * @return Return the new element added.
	 */
	public Entry<E> addFirst(E e) {
		Entry<E> newEntry = new Entry<E>(e, header.next, header);
		newEntry.previous.next = newEntry;
		newEntry.next.previous = newEntry;
		size++;
		return newEntry;
	}

	/**
	 * Add <code>element</code> after <code>e</code>.
	 * 
	 * @return Return the new element added.
	 */
	public Entry<E> addAfter(E element, Entry<E> e) {
		Entry<E> newEntry = new Entry<E>(element, e.next, e);
		e.next = newEntry;
		newEntry.next.previous = newEntry;
		size++;
		return newEntry;
	}
	
	/**
	 * Add <code>element</code> before <code>e</code>.
	 * 
	 * @return Return the new element added.
	 */
	public Entry<E> addBefore(E element, Entry<E> e) {
		Entry<E> newEntry = new Entry<E>(element, e, e.previous);
		e.previous = newEntry;
		newEntry.previous.next = newEntry;
		size++;
		return newEntry;
	}

	/**
	 * Remove the element <code>e</code> and return the previous element.
	 * 
	 * @return If <code>e</code> is the first element or <code>e</code> is null,
	 * 		   return null; else return the previous element of <code>e</code>.
	 */
	public Entry<E> removeAndReturnPrevious(Entry<E> e){
		if(e == null)
			return null;
		Entry<E> pre = e.previous;
		pre.next = e.next;
		e.next.previous = pre;
		e.next = e.previous = null;
		e.element = null;
		e = null;
		size--;
		return pre!=header ? pre : null;
	}
	
	/**
	 * Remove the element <code>e</code> and return the next element.
	 * 
	 * @return If <code>e</code> is the last element or <code>e</code> is null,
	 * 		   return null; else return the next element of <code>e</code>.
	 */
	public Entry<E> removeAndReturnNext(Entry<E> e){
		if(e == null)
			return null;
		Entry<E> next = e.next;
		next.previous = e.previous;
		e.previous.next = next;
		e.next = e.previous = null;
		e.element = null;
		e = null;
		size--;
		return next!=header ? next : null;
	}

	/**
	 * Get the previous element of <code>e</code>.
	 * 
	 * @return Null if <code>e</code> is the header, the previous element of
	 *         <code>e</code> else.
	 */
	public Entry<E> getPrevious(Entry<E> e) {
		return e.previous != header ? e.previous : null;
	}

	/**
	 * Get the next element of <code>e</code>.
	 * 
	 * @param e
	 * @return Null if <code>e</code> is the tail, the next element of
	 *         <code>e</code> else.
	 */
	public Entry<E> getNext(Entry<E> e) {
		return e.next != header ? e.next : null;
	}

	/**
	 * Get number of elements of this list.
	 * 
	 * @return
	 */
	public int size() {
		return size;
	}

	public static class Entry<E> {
		E element;
		Entry<E> next;
		Entry<E> previous;

		Entry(E element, Entry<E> next, Entry<E> previous) {
			this.element = element;
			this.next = next;
			this.previous = previous;
		}

		public E getElement() {
			return element;
		}
	}

	public static void main(String[] args) {
		LinkedArrayList<Student> lal1 = new LinkedArrayList<Student>();
		lal1.addLast(new Student("Zhou", 22));
		lal1.addLast(new Student("Cheng", 22));
		lal1.addLast(new Student("Xiang", 21));
		lal1.addLast(new Student("Yan", 21));
		lal1.addLast(new Student("Pan", 22));
		lal1.addLast(new Student("Wang", 22));

		Entry<Student> e1 = lal1.getFirst();

		int age1 = 10;

		while (e1 != null) {
			System.out.println(e1.getElement());
			if (e1.getElement().age == 22) {
				e1 = lal1.removeAndReturnPrevious(e1);
				if(e1 == null) { //e1 is the first element
					Entry<Student> e2 = lal1.addFirst(new Student("stu" + (age1 / 10), age1));
					lal1.addAfter(new Student("student" + (age1 / 10), age1), e2);
					age1 += 10;
					e1 = lal1.getFirst();
					continue;
				}
				Entry<Student> e2 = lal1.addAfter(new Student("stu" + (age1 / 10), age1), e1);
				lal1.addAfter(new Student("student" + (age1 / 10), age1), e2);
				age1 += 10;
			}
			e1 = lal1.getNext(e1);
		}

		System.out.println();
		System.out.println();

		e1 = lal1.getFirst();

		while (e1 != null) {
			System.out.println(e1.getElement());
			e1 = lal1.getNext(e1);
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		
//////////////////////////////////////////////////////////////
		LinkedArrayList<Student> lal2 = new LinkedArrayList<Student>();
		lal2.addLast(new Student("Zhou", 22));
		lal2.addLast(new Student("Cheng", 22));
		lal2.addLast(new Student("Xiang", 21));
		lal2.addLast(new Student("Yan", 21));
		lal2.addLast(new Student("Pan", 22));
		lal2.addLast(new Student("Wang", 22));
		Entry<Student> e2 = lal2.getLast();

		int age2 = 10;

		while (e2 != null) {
			System.out.println(e2.getElement());
			if (e2.getElement().age == 22) {
				e2 = lal2.removeAndReturnNext(e2);
				if(e2 == null) { //e2 is the last element
					Entry<Student> e3 = lal2.addLast(new Student("stu" + (age2 / 10), age2));
					lal2.addBefore(new Student("student" + (age2 / 10), age2), e3);
					age2 += 10;
					e2 = lal2.getLast();
					continue;
				}
				Entry<Student> e3 = lal2.addBefore(new Student("stu" + (age2 / 10), age2), e2);
				lal2.addBefore(new Student("student" + (age2 / 10), age2), e3);
				age2 += 10;
			}
			e2 = lal2.getPrevious(e2);
		}

		System.out.println();
		System.out.println();

		e2 = lal2.getLast();

		while (e2 != null) {
			System.out.println(e2.getElement());
			e2 = lal2.getPrevious(e2);
		}
		System.out.println( -2%3 + ", " + -3%3 + ", " + -1%3);
	}
}

class Student {
	String name;
	int age;

	Student(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String toString() {
		return "Your name is " + name + ", next year you will be " + (age + 1)
				+ " years old!";
	}
}
import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A custom List implementation where elements are stored in a series of linked nodes,
 * each capable of holding multiple items. This structure provides better memory locality
 * and fewer pointer dereferences compared to traditional singly-linked lists. When elements
 * are added or removed, the list automatically redistributes items between neighboring nodes
 * to ensure that every node (except possibly the last one) remains at least half full,
 * maintaining efficient use of space and predictable traversal performance.
 */

public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E> {
	/**
	 * Default number of elements that may be stored in each node.
	 */
	private static final int DEFAULT_NODESIZE = 4;

	/**
	 * Number of elements that can be stored in each node.
	 */
	private final int nodeSize;

	/**
	 * Dummy node for head. It should be private but set to public here only for
	 * grading purpose. In practice, you should always make the head of a linked
	 * list a private instance variable.
	 */
	public Node head;

	/**
	 * Dummy node for tail.
	 */
	private Node tail;

	/**
	 * Number of elements in the list.
	 */
	private int size;

	/**
	 * Constructs an empty list with the default node size.
	 */
	public StoutList() {
		this(DEFAULT_NODESIZE);
	}

	/**
	 * Constructs an empty list with the given node size.
	 * 
	 * @param nodeSize number of elements that may be stored in each node, must be
	 *                 an even number
	 */
	public StoutList(int nodeSize) {
		if (nodeSize <= 0 || nodeSize % 2 != 0)
			throw new IllegalArgumentException();

		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		this.nodeSize = nodeSize;
	}

	/**
	 * Constructor
	 * 
	 * @param head
	 * @param tail
	 * @param nodeSize
	 * @param size
	 */
	public StoutList(Node head, Node tail, int nodeSize, int size) {
		this.head = head;
		this.tail = tail;
		this.nodeSize = nodeSize;
		this.size = size;
	}

	@Override
	public int size() {

		return size;
	}

	@Override
	public boolean add(E item) {
	   
		
	    if (item == null) {
	        throw new NullPointerException();
	    }
	    
	    
	    if (contains(item)) {
	        return false;
	    }
	    
	    Node nNode = new Node();
	    nNode.addItem(item);

	    if (size == 0) {
	        head.next = nNode;
	        nNode.previous = head;
	        nNode.next = tail;
	        tail.previous = nNode;
	    } else {
	        if (tail.previous.count < nodeSize) {
	            tail.previous.addItem(item);
	        } else {
	            Node lastNode = tail.previous;
	            lastNode.next = nNode;
	            nNode.previous = lastNode;
	            nNode.next = tail;
	            tail.previous = nNode;
	        }
	    }

	    size++;

	    return true;
	    
	}


	@Override
	public void add(int pos, E item) {
		if (pos < 0 || pos > size) {
            throw new IndexOutOfBoundsException();
        }

        if (item == null) {
            throw new NullPointerException();
        }

        if (size == 0 || pos == size) {
            add(item);
            return;
        }

        NodeInfo info = find(pos);
        Node temp = info.node;
        int offset = info.offset;

        if (temp.count < nodeSize) {
            temp.addItem(offset, item);
        } else {
            Node nNode = new Node();
            int split = nodeSize / 2;

            for (int i = split; i < nodeSize; i++) {
                nNode.addItem(temp.data[split]);
                temp.removeItem(split);
            }

            nNode.next = temp.next;
            nNode.previous = temp;
            temp.next.previous = nNode;
            temp.next = nNode;

            if (offset <= split) {
                temp.addItem(offset, item);
            } else {
                nNode.addItem(offset - split, item);
            }
        }

        size++;
	}

	  
	@Override
	public E remove(int pos) {
		
		if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException();
        }

        NodeInfo nodeInfo = find(pos);
        Node nodeToRemove = nodeInfo.node;
        int offset = nodeInfo.offset;
        E removedItem = nodeToRemove.data[offset];

        if (nodeToRemove.count > nodeSize / 2 || nodeToRemove.next == tail) {
            nodeToRemove.removeItem(offset);
        } else {
            Node nextNode = nodeToRemove.next;

            if (nextNode.count > nodeSize / 2) {
                nodeToRemove.addItem(nextNode.data[0]);
                nextNode.removeItem(0);
            } else {
                for (int i = 0; i < nextNode.count; i++) {
                    nodeToRemove.addItem(nextNode.data[i]);
                }
                nodeToRemove.next = nextNode.next;
                nextNode.next.previous = nodeToRemove;
            }
        }

        size--;
        return removedItem;
		
		

	}

	/**
 	* Sorts all elements of the StoutList into non-decreasing order.
 	* All elements are first copied into an array, sorted using insertionSort, and then rebuilt into full nodes (except possibly the last).
 	* This approach relies on a Comparator<E> for insertionSort and ensures that after sorting, each node is filled to capacity.
 	*/
	public void sort() {
		E[] Sort = (E[]) new Comparable[size];
		int index = 0;
		Node nTail = head.next;

		while (nTail != tail) {
			for (int i = 0; i < nTail.count; i++) {
				Sort[index] = nTail.data[i];
				index++;
			}
			nTail = nTail.next;
		}

		head.next = tail;
		tail.previous = head;
		insertionSort(Sort, new Ecomparator());
		size = 0;

		for (int i = 0; i < Sort.length; i++) {
			add(Sort[i]);
		}
	}

	/**
 	* Sorts all elements of the StoutList into non-increasing order using bubbleSort.
	* After sorting, each node (except possibly the last) is completely filled with elements.
 	* This operation requires Comparable<? super E> for bubbleSort to function.
 	*/
	public void sortReverse() {
		E[] ReverseSort = (E[]) new Comparable[size];

		int index = 0;
		Node nTail = head.next;
		
		while (nTail != tail) {
			for (int i = 0; i < nTail.count; i++) {
				ReverseSort[index] = nTail.data[i];
				index++;
			}
			nTail = nTail.next;
		}

		head.next = tail;
		tail.previous = head;
		bubbleSort(ReverseSort);
		size = 0;
		for (int i = 0; i < ReverseSort.length; i++) {

			add(ReverseSort[i]);
		}
	}

	@Override
	public Iterator<E> iterator() {

		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator() {

		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {

		return new StoutListIterator(index);
	}

	private NodeInfo find(int pos) {
		
		Node find = head.next;
		int findPosition = 0;
		while (find != tail) {
			if (findPosition + find.count <= pos) {
				findPosition += find.count;
				find = find.next;
				continue;
			}
			NodeInfo nodeInfo = new NodeInfo(find, pos - findPosition);
			return nodeInfo;
		}
		return null;

	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes.
	 */
	public String toStringInternal() {
		return toStringInternal(null);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes and the position of the iterator.
	 *
	 * @param iter an iterator for this list
	 */
	public String toStringInternal(ListIterator<E> iter) {
		int count = 0;
		int position = -1;
		if (iter != null) {
			position = iter.nextIndex();
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Node current = head.next;
		while (current != tail) {
			sb.append('(');
			E data = current.data[0];
			if (data == null) {
				sb.append("-");
			} else {
				if (position == count) {
					sb.append("| ");
					position = -1;
				}
				sb.append(data.toString());
				++count;
			}

			for (int i = 1; i < nodeSize; ++i) {
				sb.append(", ");
				data = current.data[i];
				if (data == null) {
					sb.append("-");
				} else {
					if (position == count) {
						sb.append("| ");
						position = -1;
					}
					sb.append(data.toString());
					++count;

					if (position == size && count == size) {
						sb.append(" |");
						position = -1;
					}
				}
			}
			sb.append(')');
			current = current.next;
			if (current != tail)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Node type for this list. Each node holds a maximum of nodeSize elements in an
	 * array. Empty slots are null.
	 */
	private class Node {
		/**
		 * Array of actual data elements.
		 */
		// Unchecked warning unavoidable.
		public E[] data = (E[]) new Comparable[nodeSize];

		/**
		 * Link to next node.
		 */
		public Node next;

		/**
		 * Link to previous node;
		 */
		public Node previous;

		/**
		 * Index of the next available offset in this node, also equal to the number of
		 * elements in this node.
		 */
		public int count;

		/**
		 * Adds an item to this node at the first available offset. Precondition: count
		 * < nodeSize
		 * 
		 * @param item element to be added
		 */
		void addItem(E item) {
			if (count >= nodeSize) {
				return;
			}
			data[count++] = item;
			// useful for debugging
			// System.out.println("Added " + item.toString() + " at index " + count + " to
			// node " + Arrays.toString(data));
		}

		/**
		 * Adds an item to this node at the indicated offset, shifting elements to the
		 * right as necessary.
		 * 
		 * Precondition: count < nodeSize
		 * 
		 * @param offset array index at which to put the new element
		 * @param item   element to be added
		 */
		void addItem(int offset, E item) {
			if (count >= nodeSize) {
				return;
			}
			for (int i = count - 1; i >= offset; --i) {
				data[i + 1] = data[i];
			}

			++count;
			data[offset] = item;
		}

		/**
		 * Deletes an element from this node at the indicated offset, shifting elements
		 * left as necessary. Precondition: 0 <= offset < count
		 * 
		 * @param offset
		 */
		void removeItem(int offset) {


			for (int i = offset + 1; i < nodeSize; ++i) {
				data[i - 1] = data[i];
			}
			data[count - 1] = null;
			--count;
		}
	}

	private class StoutListIterator implements ListIterator<E> {

		int curr = 0;

		int last;

		public E[] data;

		boolean prev;

		/**
		 * Default constructor
		 */
		public StoutListIterator() {
			
			curr = 0;
			last = 0;
			prev = false;
			int index = 0;
			
			data = (E[]) new Comparable[size];

			Node nodeData = head.next;
			while (nodeData != tail) {
				for (int i = 0; i < nodeData.count; i++) {
					data[index] = nodeData.data[i];
					index++;
				}
				nodeData = nodeData.next;
			}

		}

		/**
		 * Constructor finds node at a given position.
		 * 
		 * @param pos
		 */
		public StoutListIterator(int pos) {
			curr = pos;
			last = 0;
			data = (E[]) new Comparable[size];

			int index = 0;

			Node nodeData = head.next;
			while (nodeData != tail) {
				for (int i = 0; i < nodeData.count; i++) {
					data[index] = nodeData.data[i];
					index++;
				}
				nodeData = nodeData.next;
			}

		}

		@Override
		public boolean hasNext() {
			return curr < size;
		}

		@Override
		public E next() {
			if (hasNext()) {
				E element = data[curr];
				curr++;
				last = 1;
				prev = false;
				return element;
			}
			else {
				throw new NoSuchElementException();
			}
		}

		@Override
		/**
		 * remove the current node from the list
		 */
		public void remove() {
			if (last == 0) {
				throw new IllegalStateException();
			}
			else if (prev && last == 1) {
				
				StoutList.this.remove(curr);
				
				int index = 0;
				
				data = (E[]) new Comparable[size];

				Node nodeData = head.next;
				while (nodeData != tail) {
					for (int i = 0; i < nodeData.count; i++) {
						data[index] = nodeData.data[i];
						index++;
					}
					nodeData = nodeData.next;
				}
				
				last = 0;
				
				curr--;
				
				if (curr < 0) {
					curr  = 0;
				}
			}
			
			else if (!prev && last == 1) {
				StoutList.this.remove(curr - 1);
				
				int index = 0;
				
				data = (E[]) new Comparable[size];

				Node nodeData = head.next;
				while (nodeData != tail) {
					for (int i = 0; i < nodeData.count; i++) {
						data[index] = nodeData.data[i];
						index++;
					}
					nodeData = nodeData.next;
				}
				
				last = 0;
			}

		}

		@Override
		public boolean hasPrevious() {

			return curr > 0;
		}

		@Override
		public E previous() {
			if (hasPrevious()) {
				curr--;
				E element = data[curr];
				last = 1;
				prev = true;
				return element;
			}
			else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public int nextIndex() {

			return curr;
		}

		@Override
		public int previousIndex() {

			return curr - 1;
		}

		@Override
		public void set(E e) {
			if (prev && last == 1) {
				
				NodeInfo temp = find(curr);
				temp.node.data[temp.offset] = e;
				data[curr] = e;
				last = 0;
			}
			else if (!prev && last == 1) {
				
				NodeInfo temp = find(curr - 1);
				temp.node.data[temp.offset] = e;
				data[curr - 1] = e;
				last = 0;
			}
			else {
				throw new IllegalStateException();
			}

		}

		@Override
		public void add(E e) {

			if (e != null) {
				
				StoutList.this.add(curr, e);
				curr++;
				int index = 0;
				
				data = (E[]) new Comparable[size];

				Node nodeData = head.next;
				while (nodeData != tail) {
					for (int i = 0; i < nodeData.count; i++) {
						data[index] = nodeData.data[i];
						index++;
					}
					nodeData = nodeData.next;
				}
				
				last = 0;
			}
			else {
				throw new NullPointerException();
			}

		}
		
	}

	/**
	 * Sorts an array arr[] using the insertion sort algorithm in the NON-DECREASING
	 * order.
	 * 
	 * @param arr  array storing elements from the list
	 * @param comp comparator used in sorting
	 */
	private void insertionSort(E[] arr, Comparator<? super E> comp) {
		
		for (int i = 1; i < arr.length; i++) {
			E key = arr[i];
			int j = i - 1;
			
			while (j >= 0 && comp.compare(arr[j], key) > 0) {
				arr[j + 1] = arr[j];
				j--;
			}
			arr[j + 1] = key;
			}
	}

	/**
	 * Sorts arr[] using the bubble sort algorithm in the NON-INCREASING order.
	 * @param arr array holding elements from the list
	 */
	private void bubbleSort(E[] arr) {
		int n = arr.length;
		
		for (int i = 0; i < n - 1; i++) {
			boolean swap = false;
			for (int j = 0; j < n - i - 1; j++) {
				if (arr[j].compareTo(arr[j + 1]) < 0) {
					E temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
					swap = true;	
				}
			}
			if (!swap) {
				break;
			}
		}
	}

	// this class finds info on a node
	private class NodeInfo {
		
		public Node node;
		public int offset;
		
		public NodeInfo(Node node, int offset) {
			
			this.node = node;
			this.offset = offset;

		}

	}

	class Ecomparator<E extends Comparable<E>> implements Comparator<E> {

		@Override
		public int compare(E o1, E o2) {
			return o1.compareTo(o2);
		}

	}
}
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;


public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {

    //initialize dummy head node, max height of 4, and size
    private SkipListSetItem<T> head;
    private int maxHeight = 4;
    private int size = 0;

    //iterator implementing iterator interface class
    private class SkipListSetIterator implements Iterator<T>{
        private SkipListSetItem<T> curr;

        //constructor
        public SkipListSetIterator(){
            curr = head;
        }

        //hasNext method from Iterator
        @Override
        public boolean hasNext() {
            
            if(curr != null && curr.pointers.get(0) != null){
                return true;
            }

            return false;
        }

        //next method from Iterator
        @Override
        public T next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            else{
                curr =curr.pointers.get(0);
                return curr.data;
            }

        }

        //remove method from Iterator
        @Override
        public void remove(){
            if(curr == head || curr == null){
                throw new IllegalStateException();
            }

            SkipListSet.this.remove(curr.data);

            curr = head;
        }
    }

    //skip list item class
    private class SkipListSetItem<T extends Comparable<T>>{
        //arraylist of pointers for an item stores neighboring nodes at level i
        T data;
        ArrayList<SkipListSetItem<T>> pointers;

        //constructor
        public SkipListSetItem(T payload, int levels){
            data = payload;
            pointers = new ArrayList<>(levels);

            for(int i=0;i<levels;i++){
                pointers.add(null);
            }

        }

        //returns the adjacent node on that level
        public SkipListSetItem<T> next(int level){
		if (level < 0 || level > maxHeight-1)
			return null;

		return pointers.get(level);
	    }

    }

    //empty constructor
    public SkipListSet(){
        //initial max height of 4
        head = new SkipListSetItem<T>(null, maxHeight);
        size = 0;

    }

    //constructor with input 
    public SkipListSet(Collection<? extends T> c){
        this();
        for(T elem : c){
            add(elem);
        }
    }
    
    //returns number of elements in skip list, size
    @Override
    public int size() {
        return size;
    }

    //returns true if skip list is empty
    @Override
    public boolean isEmpty() {
        if(size == 0){
            return true;
        }

        return false;
    }

    //method that checks if an object is in skip list
    @Override
    public boolean contains(Object o) {
        int level = maxHeight -1;
        SkipListSetItem<T> curr = head;
        SkipListSetItem<T> neighbor = head.next(level);

        //if object is null throw exception
        if(o == null){
            throw new NullPointerException();
        }
        if (!(o instanceof Comparable<?>)) {
            throw new ClassCastException();
        }

        @SuppressWarnings("unchecked")
        T target = (T) o;

        //iterate until base level of 0
        while(level>=0){
            //if neighbor is not null and target is greater than neighbor, go right
            while(neighbor != null && target.compareTo(neighbor.data) > 0){
                curr = neighbor;
                neighbor = curr.next(level);
            }

            //found target value, return true
            if(neighbor != null && target.compareTo(neighbor.data) == 0){
                return true;
            }
            //decrease level, update neighbor
            level--;
            neighbor = curr.next(level);
        }

        return false;

    }

    //iterator method
    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator();
    }

    //returns all elements in the skip list in an array
    @Override
    public Object[] toArray() {
        Object[] res = new Object[size];
        int i =0;
        SkipListSetItem<T> curr = head.pointers.get(0);

        //iterate through bottom level of skip list until null
        while(curr != null){
            res[i] = curr.data;
            curr = curr.pointers.get(0);
            i++;
        }
        return res;

    }

    //toArray typed method
    @SuppressWarnings("unchecked")
    @Override
    public <U> U[] toArray(U[] a) {
        if(a == null){
            throw new NullPointerException();
        }

        //case when a is smaller than size of skiplist
        if (a.length < size) { 
            a = (U[]) Array.newInstance(a.getClass().getComponentType(), size);
        } 
        else if (a.length > size) {
            a[size] = null;
        }

        int i = 0;
        SkipListSetItem<T> curr = head.pointers.get(0);

        while(curr != null){
            a[i] = (U) curr.data;
            i++;
            curr = curr.pointers.get(0);
        }

        return a;

    }

    //add method
    @Override
    public boolean add(T e) {
        SkipListSetItem<T> curr = head;
        int level = maxHeight - 1;
        SkipListSetItem<T> neighbor = head.next(level);
        int newHeight = randHeight();
        SkipListSetItem<T> newNode = new SkipListSetItem<>(e,newHeight);
    
        //if element is null
        if(e == null){
            throw new NullPointerException();
        }

        //if element is already in skip list
        if(this.contains(e)){
            return false;
        }

        //if the current size is equal to 2^maxheight + 1, increase maxheight
        if(size == Math.pow(2,maxHeight) +1){
            maxHeight++;
            head.pointers.add(null);
        }
        

        while(level>=0){
            //go right if neighbor is not null and elements data is greater than neighbors data
            while(neighbor != null && newNode.data.compareTo(neighbor.data) > 0){
                curr = neighbor;
                neighbor = curr.next(level);
            }

            //if below newHeight for new node, set pointers to and from the new ndoe
            if(level < newHeight){
                newNode.pointers.set(level,neighbor);
                curr.pointers.set(level,newNode);
            }
            
            //decrease level
            level--;
            neighbor = curr.next(level);
        }
        
        //update size and return true
        size++;
        return true;
    }

    //method to randomize height where 0 is heads and 1 is tails
    public int randHeight(){
        int coin;
        Random rand = new Random();
        int i = 1;

        while(i < maxHeight){
            coin = rand.nextInt(2);
            if(coin == 1){
                return i;
            }
            else{
                i ++;
            }
        }

        return i;
    }

    //method to rebalance skip list
    public void reBalance(){

        //convert skip list to array, clear skip list, add elements from array into skip list
        @SuppressWarnings("unchecked")
        T[] res = (T[]) new Comparable[size];
        res = this.toArray(res);

        this.clear();

        for(T n: res){
            this.add(n);
        }
    }

    //method to remove
    @Override
    public boolean remove(Object o) {
        SkipListSetItem<T> curr = head;
        int level = maxHeight -1;
        SkipListSetItem<T> neighbor = head.next(level);

        //suppress unchecked warnings
        @SuppressWarnings("unchecked")
        T target = (T) o;

        //if element is null
        if(target == null){
            throw new NullPointerException();
        }

        //if element is not in skip list
        if(!this.contains(target)){
            return false;
        }
        
        //iterate from top to bottom level
        while(level >= 0){

            //while neighbor is not null and target value is greater than neighbor value, go right
            while(neighbor != null && target.compareTo(neighbor.data) > 0){
                curr = neighbor;
                neighbor = neighbor.next(level);
            }

            //if the neighbor is my target node, set the current nodes pointers to the neighbors next node
            if(neighbor != null && target.compareTo(neighbor.data) == 0){
                curr.pointers.set(level,neighbor.next(level));
            }

            //decrease level
            level--;
            neighbor = curr.next(level);

        }

        //decrease size
        size--;
        return true;
    }

    //method to check if all elements are present in skip list
    @Override
    public boolean containsAll(Collection<?> c) {

        if(c == null){
            throw new NullPointerException();
        }

        //call contains on all elements of collection
        for(Object n: c){
            if(n == null){
                throw new NullPointerException();
            }
            if(contains(n) == false){
                return false;
            }
        }
        return true;
    }

    //method to add all provided elements to skip list
    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean res = false;
        if(c == null){
            throw new NullPointerException();
        }
        for(T n: c){
            if(n == null){
                throw new NullPointerException();
            }
            //if set is modified, return true
            if(add(n) == true){
                res = true;
            }
        }

        return res;
    }

    //retainAll method
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean res = false;

        //if collection is null
        if(c == null){
            throw new NullPointerException();
        }

        //iterate through skip list, if element is not in c, add it to a list to be removed
        List<T> removeList = new ArrayList<>();

        for (T element : this) {
            if(element == null){
                throw new NullPointerException();
            }
            if (!c.contains(element)) {
                removeList.add(element);
                res = true;
            }
        }

        //remove all elements from the removeList
        for (T element : removeList) {
            this.remove(element);  
        }
            
        return res;
    }

    //method to remove all elements from collection c
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean res = false;
        if(c == null){
            throw new NullPointerException();
        }
        for(Object n:c){
            if(n == null){
                throw new NullPointerException();
            }
            //set is modified as a result of this, return true
            if(remove(n) == true){
                res = true;
            }
        }

        return res;

    }

    //clear skip list method
    @Override
    public void clear() {
        //set head to null to clear current skip list, re instantiate head in case of rebalance happening, reset size and maxHeight
        head = null;
        head = new SkipListSetItem<T>(null, maxHeight);
        size = 0;
        maxHeight = 4;
    }

    //only returns null as specified in documentation
    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    //throws UnsupportedOperationException
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException();
    }

    //throws UnsupportedOperationException
    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException();
    }

    //throws UnsupportedOperationException
    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException();
    }

    //returns first element in the skip list
    @Override
    public T first() {
        //if skiplist is empty throw exception, else return leftmost lowest data 
        if(size == 0){
            throw new NoSuchElementException();
        }
        else{
            return head.pointers.get(0).data;
        }
    
    }

    //returns the last element in the skip list
    @Override
    public T last() {
        //if skiplist is empty throw exception, else return rightmost data
        if(size == 0){
            throw new NoSuchElementException();
        }
        else{
            SkipListSetItem<T> curr = head;
            while (curr.pointers.get(0) != null) {
                curr = curr.pointers.get(0);
            }

            return curr.data;
        }

    }

    //hashcode method
    public int hashCode(){
        //iterate through all elements and sum their hashcodes
        int res = 0;

        for(T elem : this){
            res += elem.hashCode();
        }

        return res;
    }

    //equals method
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        //if o is not a set, return false
        if(o == null || !(o instanceof Set)){
            return false;
        }

        Set<?> obj = (Set<?>) o;

        //if sizes are not equal return false
        if(this.size() != obj.size()){
            return false;
        }
        
        //returns true if both sets have same elements
        return this.containsAll(obj) && obj.containsAll(this);

    }

}


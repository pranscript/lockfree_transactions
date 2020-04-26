import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

public class Execution{

	Stack<Descriptor> helpStack = new Stack<Descriptor>();
	ArrayList<NodeWrapper> delNodes = new ArrayList<NodeWrapper>();
	public void create(Transaction tr) {
		executeTransaction(tr.nodeWrapper[0].info.get().desc);
		//boolean flag = executeTransaction(tr);
		
	}
	
	public boolean executeTransaction(Descriptor desc) {
		
		EXECUTEOPS(desc, 0);
		return false;
		
	}
	
	public boolean EXECUTEOPS(Descriptor desc, int opID) {
		
		boolean ret = true;
		if(helpStack.contains(desc)) {
			desc.status.compareAndSet(TxStatus.ACTIVE, TxStatus.ABORTED);
			return false;
		}
		helpStack.push(desc);
		while(desc.status.get()==TxStatus.ACTIVE && ret && opID<desc.size) {
			OperationNode op = desc.ops.get(opID);
			if(op.operation == OpType.FIND) {
				ret = FIND(op.key,desc,opID);
			}
			else if(op.operation == OpType.INSERT) {
				ret = INSERT(op.key,desc,opID);
			}
			else if(op.operation == OpType.DELETE) {
				NodeWrapper del = new NodeWrapper();
				ret = DELETE(op.key, desc, opID, del);
				delNodes.add(del);
			}
			opID++;
		}
		
		helpStack.pop();
		if(ret==true) {
			if(desc.status.compareAndSet(TxStatus.ACTIVE, TxStatus.COMMITTED)) {
				MARKDELETE(delNodes,desc);
			}
		}
		else {
			return desc.status.compareAndSet(TxStatus.ACTIVE, TxStatus.ABORTED);
		}
		return true;
	}
	
	public boolean FIND(int key, Descriptor desc, int opID) {
		AtomicReference<NodeInfo> info = new AtomicReference<NodeInfo>();
		info.get().desc = desc;
		info.get().opID = opID;
		boolean ret = true;
		
		while(ret) {
			NodeWrapper curr = DO_LOCATEPRED(key);
			if(IsNODEPRESENT(curr, key)) {
				ret = UPDATEINFO(curr, info, true);
			}
			else {
				ret = false;
			}
		}
		return ret;
		
	}
	
	public boolean INSERT(int key, Descriptor desc, int opID) {
		AtomicReference<NodeInfo> info = new AtomicReference<NodeInfo>();
		info.set(new NodeInfo());
		info.get().desc = desc;
		info.get().opID = opID;
		boolean ret = true;
		while(ret) {
			NodeWrapper curr = DO_LOCATEPRED(key);
			if(IsNODEPRESENT(curr, key)) {
				ret = UPDATEINFO(curr, info, false);
				break;
			}
			else {
				NodeWrapper n = new NodeWrapper();
				n.key = key;
				n.info.set(info.get());
				ret = DO_INSERT(n);
				break;
			}
		}
		return ret;
	}
	
	public boolean DELETE(int key, Descriptor desc, int opID, NodeWrapper del) {
		
		AtomicReference<NodeInfo> info = new AtomicReference<NodeInfo>();
		info.get().desc = desc;
		info.get().opID = opID;
		boolean ret = true;
		
		while(true) {
			NodeWrapper curr = DO_LOCATEPRED(key);
			if(IsNODEPRESENT(curr, key)) {
				ret = UPDATEINFO(curr, info, true);
			}
			else {
				ret = false;
			}
			if(ret == true) {
				delNodes.add(curr);
				return true;
			}
			else if(ret == false) {
				return false;
			}
		}
	}
	
	public boolean MARKDELETE(ArrayList<NodeWrapper> delNodes, Descriptor desc) {
		for (NodeWrapper del : delNodes) {
			AtomicReference<NodeInfo> info = new AtomicReference<NodeInfo>();
            info = del.info;
            
            if(del.info.get()==info.get()) {
            	if(del.info.compareAndSet(info.get(), SETMARK(del))) {
            		DO_DELETE(del);
            		return true;
            	}
            	else {
            		return false;
            	}
            }
    	} 
		return false;
	}
	
	public boolean IsNODEPRESENT(NodeWrapper n, int key) {
		return n.key==key;
	}
	
	public boolean IsKEYPRESENT(AtomicReference<NodeInfo> info, Descriptor desc) {
		OpType op = info.get().desc.ops.get(info.get().opID).operation;
		AtomicReference<TxStatus> status = new AtomicReference<TxStatus>();
		status = info.get().desc.status;
		
		switch(status.get()) {
		case ACTIVE:
			if(info.get().desc ==desc) {
				return op == OpType.FIND || op == OpType.INSERT;
			}
			else {
				return op == OpType.FIND || op == OpType.DELETE;
			}
		case COMMITTED:
			return op == OpType.FIND || op == OpType.INSERT;
		case ABORTED:
			return op == OpType.FIND || op == OpType.DELETE;
		}
		return true;
	}
	
	public boolean UPDATEINFO(NodeWrapper n, AtomicReference<NodeInfo> info, boolean wantKey){
		
		AtomicReference<NodeInfo> oldInfo = new AtomicReference<NodeInfo>();
		oldInfo.set(n.info.get());
		if(IsMARKED(oldInfo)) {
			DO_DELETE(n);
			return false;
		}
		
		if(oldInfo.get().desc != info.get().desc) {
			EXECUTEOPS(oldInfo.get().desc, oldInfo.get().opID+1);
		}
		else if(oldInfo.get().opID>=info.get().opID) {
			return true;
		}
		
		boolean hasKey = IsKEYPRESENT(oldInfo, oldInfo.get().desc);
		if((!hasKey && wantKey) || hasKey && !wantKey) {
			return false;
		}
		if(info.get().desc.status.get() != TxStatus.ACTIVE) {
			return false;
		}
		if(n.info.compareAndSet(oldInfo.get(), info.get())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void DO_DELETE(NodeWrapper del) {
		Iterator<NodeWrapper> itr = delNodes.iterator();
		while (itr.hasNext()) 
        { 
            NodeWrapper x = (NodeWrapper) itr.next(); 
            if(x==del) itr.remove(); 
        } 
	}
	
	public NodeInfo SETMARK(NodeWrapper del) {
		
		AtomicReference<NodeInfo> curr = new AtomicReference<NodeInfo>();
		boolean flag = true;
		while(flag) {
			if(del.markRef.attemptMark(del.info.get(), true)) {
				flag=false;
			}
		}
		
		return curr.get();
	}
	
	public boolean IsMARKED(AtomicReference<NodeInfo> info) {
		
		Iterator<NodeWrapper> itr = delNodes.iterator();
		while (itr.hasNext()) 
        { 
            NodeWrapper x = (NodeWrapper) itr.next(); 
            if(x.info == info && x.markRef.isMarked()) return true; 
        } 
		
		return true;
	}
	
	public NodeWrapper DO_LOCATEPRED(int key) {
		NodeWrapper curr  = LockFreeLinkedList.head.get();
		//int i=0;
		while(curr!=null) {
			if(curr.key == key) {
				return curr;
			}
			curr = curr.next;
		}
		
		
		return new NodeWrapper();
	}
	
	public boolean DO_INSERT(NodeWrapper n) {
		
		if(LockFreeLinkedList.head==null) {
			LockFreeLinkedList.head.set(n);
			LockFreeLinkedList.tail.set(n);
		}
		else {
			LockFreeLinkedList.tail.get().next=n;
			LockFreeLinkedList.tail.set(n);
		}
		return true;
	}
	
}













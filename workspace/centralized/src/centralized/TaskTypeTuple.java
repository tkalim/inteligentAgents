package centralized;

import logist.task.Task;

public class TaskTypeTuple {
	public Task task;
	public String type;
	
	TaskTypeTuple(Task task, String type){
		this.task = task;
		this.type = type;
	}
	
	public TaskTypeTuple getReverseTask() {
		return new TaskTypeTuple(this.task, this.type.equals("PickUp")?"Delivery":"PickUp");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((task == null) ? 0 : task.id);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskTypeTuple other = (TaskTypeTuple) obj;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (! (task.id == other.task.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}

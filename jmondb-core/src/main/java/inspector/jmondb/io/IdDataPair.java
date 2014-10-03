package inspector.jmondb.io;

public class IdDataPair<T> {

	private Long id;
	private T data;

	public IdDataPair(Long id, T data) {
		setId(id);
		setData(data);
	}

	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public T getData() {
		return data;
	}

	private void setData(T data) {
		this.data = data;
	}
}

package pt.up.fe.specs.antbuilder;


public class Person {
	private final String name;

	public Person(String name) {
		this.name = name;

		// System.out.println(new Person2().getVersion());
	}

	public String getName() {
		return name;
	}

	public static void main(String[] args) {
		new Person("joao");
	}
}

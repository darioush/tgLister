package edu.washington.cs.tgs;

public class TT {
	public static class ParseMe {
		public static class MyFoo {

			{
				int yy = 4;
			}

			static {
				int xx = 44;
			}

			public static class MyFoo2 {

				public static class MyBoz {
					{
						new MyFoo() {
							public int xx() {
								new MyFoo();
								return 42;
							};

							public int yy() {
								new Object() {
									public int x() {
										return 20;
									}

								};

								return new Object() {
									public int x() {
										return 20;
									}

								}.x();
							};
						};
					}
				}
			}
		}

	}

}

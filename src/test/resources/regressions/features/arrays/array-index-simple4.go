package pkg

ensures a[0] == 1 && a[1] == 2
func test1() (a [2]int) {
	a[0] = 1
	a[1] = 2
}

func test2() {
	a := test1()
  assert a[0] == 1
  assert a[1] == 2
}

ensures r[0] == a
func test3(a [3]int) (r [1][3]int) {
  r[0] = a
}

func test4() {
  var a [3]int
  a[1] = 42
  b := test3(a)
  assert b[0][0] == 0
  assert b[0][1] == 42
}
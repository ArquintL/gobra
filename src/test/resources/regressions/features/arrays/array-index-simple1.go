package pkg

func test1() {
	var a [12]int
	n := a[4]
  assert n == 0
}

requires a[0] == false
func test2(a [12]bool) {
	var b bool
  b = a[0]
  assert !b
}

func test3() {
	var a [2]int
	a[1] = 12
  assert a[0] == 0
  assert a[1] == 12
}

func test4() {
  var a [3]int
	var b [3]int
	b = a
	a[2] = 42
	assert b[2] == 0
}

requires forall i int :: 0 <= i && i < len(a) ==> a[i] == 0
func test5(a [4]int) {
  b := a
  assert len(b) == 4
  assert cap(b) == 4
  a[2] = 12
  b[3] = 24
  assert b[2] == a[3] && a[3] == 0
  assert a[2] == 12 && b[3] == 24
}

requires forall i int, j int :: 0 <= i && i < 2 && 0 <= j && j < 3 ==> a[i][j] == i + j
func test6(a [2][3]int) {
  b := a
  c := b
  d := c
  assert d[1][2] == 1 + 2
}

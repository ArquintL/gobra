// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2011-2020 ETH Zurich.

package pkg;

type cell struct{
	val int;
};

requires acc(x.val) && acc(y.val);
requires x.val == a && y.val == b;
ensures  acc(x.val) && acc(y.val);
ensures  x.val == b && y.val == a;
func swap1(x, y *cell, ghost a, b int) {
	x.val, y.val = y.val, x.val;
};


requires acc(x.val) && acc(y.val);
requires x.val == a && y.val == b;
ensures  acc(x.val) && acc(y.val);
ensures  x.val == b && y.val == a;
func (x *cell) swap2(y *cell, ghost a, b int) {
    x.val, y.val = y.val, x.val;
};

func client() {
    x := cell{42};
    y := cell{17};
    swap1(&x, &y, 42, 17);
    assert x == cell{17} && y.val == 42;

    (&y).swap2(&x, 42, 17);
    assert x.val == 42 && y == cell{17};

    //:: ExpectedOutput(assert_error:assertion_error)
    assert false;
};




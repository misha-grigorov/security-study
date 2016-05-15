package com.dataart.security;

import org.junit.Assert;
import org.junit.Test;

public class Base64CoderTest {

    private ICoder coder = Base64Coder.getInstance();

    @Test
    public void testCoder() {
        String input = "So?<p>√‼\"" +
                "This 4, 5, 6, 7, 8, 9, z, {, |, } tests Base64 encoder. " +
                "Show me: @, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, " +
                "[, \\, ], ^, _, `, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s.";

        String actualResult = coder.encode(input);
        String expectedResult = "U28/PHA+4oia4oC8IlRoaXMgNCwgNSwgNiwgNywgOCwgOSwgeiwgeywgfCwgfSB0ZXN0cy" +
                "BCYXNlNjQgZW5jb2Rlci4gU2hvdyBtZTogQCwgQSwgQiwgQywgRCwgRSwgRiwgRywgSCwgSSwgSiwgSywgTCwg" +
                "TSwgTiwgTywgUCwgUSwgUiwgUywgVCwgVSwgViwgVywgWCwgWSwgWiwgWywgXCwgXSwgXiwgXywgYCwgYSwgYi" +
                "wgYywgZCwgZSwgZiwgZywgaCwgaSwgaiwgaywgbCwgbSwgbiwgbywgcCwgcSwgciwgcy4=";

        Assert.assertEquals(expectedResult, actualResult);
        Assert.assertEquals(input, coder.decode(expectedResult));
    }

    @Test
    public void testInvalidInput() {
        Assert.assertEquals(null, coder.decode("So?<p>√‼\""));
        Assert.assertEquals(null, coder.decode(null));

        Assert.assertEquals(null, coder.encode(null));
    }
}

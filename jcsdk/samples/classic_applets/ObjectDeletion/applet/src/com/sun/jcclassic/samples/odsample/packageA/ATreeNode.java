/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

package com.sun.jcclassic.samples.odsample.packageA;

import javacard.framework.JCSystem;

/**
 * Class represents nodes of a binary tree.
 */

public class ATreeNode {
    short memUsage;
    ATreeNode left = null;
    ATreeNode right = null;

    /**
     * Constructor. Makes children if depth of tree not reached maxdepth yet
     */
    public ATreeNode(short currDepth, short maxDepth) {
        memUsage = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        if (currDepth < maxDepth) {
            left = new ATreeNode((short) (currDepth + 1), maxDepth);
            right = new ATreeNode((short) (currDepth + 1), maxDepth);
        }
    }
}

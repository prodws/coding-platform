package com.github.prodws.codingplatform.problem.registration;

import com.github.prodws.codingplatform.problem.FileRole;

public record FileInput(
        String path,
        String name,
        FileRole role
) {}
